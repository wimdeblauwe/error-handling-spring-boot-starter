package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import org.springframework.boot.jackson.JsonComponent;
import org.springframework.boot.jackson.ObjectValueSerializer;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;

import java.util.List;
import java.util.Map;

@JsonComponent
public class ApiErrorResponseSerializer extends ObjectValueSerializer<ApiErrorResponse> {

    private final ErrorHandlingProperties properties;

    public ApiErrorResponseSerializer(ErrorHandlingProperties properties) {
        this.properties = properties;
    }

    @Override
    public void serializeObject(ApiErrorResponse errorResponse,
                                JsonGenerator jsonGenerator,
                                SerializationContext serializationContext) {
        if (properties.isHttpStatusInJsonResponse()) {
            jsonGenerator.writeNumberProperty("status", errorResponse.getHttpStatus().value());
        }
        ErrorHandlingProperties.JsonFieldNames fieldNames = properties.getJsonFieldNames();
        jsonGenerator.writeStringProperty(fieldNames.getCode(), errorResponse.getCode());
        jsonGenerator.writeStringProperty(fieldNames.getMessage(), errorResponse.getMessage());

        List<ApiFieldError> fieldErrors = errorResponse.getFieldErrors();
        if (!fieldErrors.isEmpty()) {
            jsonGenerator.writeArrayPropertyStart(fieldNames.getFieldErrors());
            for (ApiFieldError fieldError : fieldErrors) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringProperty(fieldNames.getCode(), fieldError.getCode());
                jsonGenerator.writeStringProperty(fieldNames.getMessage(), fieldError.getMessage());
                jsonGenerator.writeStringProperty("property", fieldError.getProperty());
                jsonGenerator.writePOJOProperty("rejectedValue", fieldError.getRejectedValue());
                jsonGenerator.writeStringProperty("path", fieldError.getPath());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }

        List<ApiGlobalError> globalErrors = errorResponse.getGlobalErrors();
        if (!globalErrors.isEmpty()) {
            jsonGenerator.writeArrayPropertyStart(fieldNames.getGlobalErrors());
            for (ApiGlobalError globalError : globalErrors) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringProperty(fieldNames.getCode(), globalError.getCode());
                jsonGenerator.writeStringProperty(fieldNames.getMessage(), globalError.getMessage());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }

        List<ApiParameterError> parameterErrors = errorResponse.getParameterErrors();
        if (!parameterErrors.isEmpty()) {
            jsonGenerator.writeArrayPropertyStart(fieldNames.getParameterErrors());
            for (ApiParameterError parameterError : parameterErrors) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringProperty(fieldNames.getCode(), parameterError.getCode());
                jsonGenerator.writeStringProperty(fieldNames.getMessage(), parameterError.getMessage());
                jsonGenerator.writeStringProperty("parameter", parameterError.getParameter());
                jsonGenerator.writePOJOProperty("rejectedValue", parameterError.getRejectedValue());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }

        for (var errorProperty : errorResponse.getProperties().entrySet()) {
            jsonGenerator.writePOJOProperty(errorProperty.getKey(), errorProperty.getValue());
        }
    }
}
