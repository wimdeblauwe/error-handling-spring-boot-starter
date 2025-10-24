package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import org.springframework.boot.jackson.JacksonComponent;
import org.springframework.boot.jackson.ObjectValueSerializer;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;

import java.util.List;

@JacksonComponent
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
                jsonGenerator.writeStartObject()
                    .writeStringProperty(fieldNames.getCode(), fieldError.getCode())
                    .writeStringProperty(fieldNames.getMessage(), fieldError.getMessage())
                    .writeStringProperty("property", fieldError.getProperty())
                    .writePOJOProperty("rejectedValue", fieldError.getRejectedValue())
                    .writeStringProperty("path", fieldError.getPath())
                    .writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }

        List<ApiGlobalError> globalErrors = errorResponse.getGlobalErrors();
        if (!globalErrors.isEmpty()) {
            jsonGenerator.writeArrayPropertyStart(fieldNames.getGlobalErrors());
            for (ApiGlobalError globalError : globalErrors) {
                jsonGenerator.writeStartObject()
                    .writeStringProperty(fieldNames.getCode(), globalError.getCode())
                    .writeStringProperty(fieldNames.getMessage(), globalError.getMessage())
                    .writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }

        List<ApiParameterError> parameterErrors = errorResponse.getParameterErrors();
        if (!parameterErrors.isEmpty()) {
            jsonGenerator.writeArrayPropertyStart(fieldNames.getParameterErrors());
            for (ApiParameterError parameterError : parameterErrors) {
                jsonGenerator.writeStartObject()
                    .writeStringProperty(fieldNames.getCode(), parameterError.getCode())
                    .writeStringProperty(fieldNames.getMessage(), parameterError.getMessage())
                    .writeStringProperty("parameter", parameterError.getParameter())
                    .writePOJOProperty("rejectedValue", parameterError.getRejectedValue())
                    .writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }

        for (var errorProperty : errorResponse.getProperties().entrySet()) {
            jsonGenerator.writePOJOProperty(errorProperty.getKey(), errorProperty.getValue());
        }
    }
}
