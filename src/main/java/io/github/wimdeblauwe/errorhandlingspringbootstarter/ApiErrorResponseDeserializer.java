package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties.JsonFieldNames;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.http.HttpStatusCode;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * This deserializer should do the inverse of ApiErrorResponseSerializer. For example, we cannot assume the
 * status field is present because the serializer doesn't always include it.
 *
 * With v4.6.0 of the starter in Spring Boot v3.x, it's possible to fully deserialize an `ApiErrorResponse` like so:
 * <pre>
 * {@code
 * ApiErrorResponse errorResponse = objectMapper.readValue(jsonString, ApiErrorResponse.class);
 * }
 * </pre>
 *
 * In the absence of this deserializer, in Spring Boot v4 the snippet above only deserializes the root properties of
 * ApiErrorResponse. Nested properties such as globalErrors and fieldErrors are not populated.
 *
 * This starter restores the ability to fully deserialize an ApiErrorResponse in Spring Boot v4 via the snippet above.
 */
@JsonComponent
public class ApiErrorResponseDeserializer extends ValueDeserializer<ApiErrorResponse> {

    private final JsonFieldNames errorFieldNames;

    public ApiErrorResponseDeserializer(ErrorHandlingProperties errorHandlingProperties) {
        this.errorFieldNames = errorHandlingProperties.getJsonFieldNames();
    }

    /**
     * Extract a simple value from a JsonNode.
     *
     * @param node
     * @return A String, Number, Boolean or null.
     */
    private Object toSimpleValue(JsonNode node) {
        return switch (node.getNodeType()) {
            case NULL, MISSING -> null;
            case BOOLEAN -> node.asBoolean();
            case NUMBER -> {
                if (node.isFloatingPointNumber()) {
                    yield node.asDouble();
                } else if (node.canConvertToInt()) {
                    yield node.asInt();
                } else {
                    yield node.asLong();
                }
            }
            case STRING -> node.asString();
            // For complex types (arrays, objects), return a string representation
            default -> node.toString();
        };
    }

    @Override
    public ApiErrorResponse deserialize(JsonParser parser, DeserializationContext ctx) throws JacksonException {
        var codeFieldName = errorFieldNames.getCode();
        var messageFieldName = errorFieldNames.getMessage();
        var statusFieldName = "status";

        JsonNode jsonRoot = parser.readValueAsTree();
        var code = jsonRoot.get(codeFieldName).asString();
        var message = jsonRoot.get(messageFieldName).asString();
        var httpStatus = jsonRoot.has(statusFieldName) ?
            HttpStatusCode.valueOf(jsonRoot.get(statusFieldName).asInt()) : null;
        var apiErrorResponse = new ApiErrorResponse(httpStatus, code, message);

        BiConsumer<String, Consumer<JsonNode>> deserializeArray = (propertyName, elementDeserializer) -> {
            if (jsonRoot.has(propertyName)) {
                jsonRoot.get(propertyName).forEach(elementDeserializer);
            }
        };

        deserializeArray.accept(errorFieldNames.getFieldErrors(), error -> {
            var errorCode = error.get(codeFieldName).asString();
            var errorMessage = error.get(messageFieldName).asString();
            var property = error.get("property").asString();
            var rejectedValue = toSimpleValue(error.get("rejectedValue"));
            var path = error.get("path").asString();
            var fieldError = new ApiFieldError(errorCode, property, errorMessage, rejectedValue, path);
            apiErrorResponse.addFieldError(fieldError);
        });

        deserializeArray.accept(errorFieldNames.getGlobalErrors(), error -> {
            var errorCode = error.get(codeFieldName).asString();
            var errorMessage = error.get(messageFieldName).asString();
            apiErrorResponse.addGlobalError(new ApiGlobalError(errorCode, errorMessage));
        });

        deserializeArray.accept(errorFieldNames.getParameterErrors(), error -> {
            var errorCode = error.get(codeFieldName).asString();
            var errorMessage = error.get(messageFieldName).asString();
            var parameter = error.get("parameter").asString();
            var rejectedValue = toSimpleValue(error.get("rejectedValue"));
            var parameterError = new ApiParameterError(errorCode, parameter, errorMessage, rejectedValue);
            apiErrorResponse.addParameterError(parameterError);
        });

        var knownRootProperties = List.of(
            statusFieldName,
            codeFieldName,
            messageFieldName,
            errorFieldNames.getFieldErrors(),
            errorFieldNames.getGlobalErrors(),
            errorFieldNames.getParameterErrors()
        );

        // Any unknown root properties must be extra properties added to the response
        // https://wimdeblauwe.github.io/error-handling-spring-boot-starter/4.6.0/#adding-extra-properties-in-the-response
        jsonRoot.propertyNames().forEach(propertyName -> {
            if (!knownRootProperties.contains(propertyName)) {
                var propertyValue = toSimpleValue(jsonRoot.get(propertyName));
                apiErrorResponse.addErrorProperty(propertyName, propertyValue);
            }
        });
        return apiErrorResponse;
    }
}
