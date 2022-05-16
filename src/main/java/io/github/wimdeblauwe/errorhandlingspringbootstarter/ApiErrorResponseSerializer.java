package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@JsonComponent
public class ApiErrorResponseSerializer extends JsonSerializer<ApiErrorResponse> {

    private final ErrorHandlingProperties properties;

    public ApiErrorResponseSerializer(ErrorHandlingProperties properties) {
        this.properties = properties;
    }

    @Override
    public void serialize(ApiErrorResponse errorResponse,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        if (properties.isHttpStatusInJsonResponse()) {
            jsonGenerator.writeNumberField("status", errorResponse.getHttpStatus().value());
        }
        ErrorHandlingProperties.JsonFieldNames fieldNames = properties.getJsonFieldNames();
        jsonGenerator.writeStringField(fieldNames.getCode(), errorResponse.getCode());
        jsonGenerator.writeStringField(fieldNames.getMessage(), errorResponse.getMessage());

        List<ApiFieldError> fieldErrors = errorResponse.getFieldErrors();
        if (!fieldErrors.isEmpty()) {
            jsonGenerator.writeArrayFieldStart(fieldNames.getFieldErrors());
            for (ApiFieldError fieldError : fieldErrors) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField(fieldNames.getCode(), fieldError.getCode());
                jsonGenerator.writeStringField(fieldNames.getMessage(), fieldError.getMessage());
                jsonGenerator.writeStringField("property", fieldError.getProperty());
                jsonGenerator.writeObjectField("rejectedValue", fieldError.getRejectedValue());
                jsonGenerator.writeObjectField("path", fieldError.getPath());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }

        List<ApiGlobalError> globalErrors = errorResponse.getGlobalErrors();
        if (!globalErrors.isEmpty()) {
            jsonGenerator.writeArrayFieldStart(fieldNames.getGlobalErrors());
            for (ApiGlobalError globalError : globalErrors) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField(fieldNames.getCode(), globalError.getCode());
                jsonGenerator.writeStringField(fieldNames.getMessage(), globalError.getMessage());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }

        List<ApiParameterError> parameterErrors = errorResponse.getParameterErrors();
        if (!parameterErrors.isEmpty()) {
            jsonGenerator.writeArrayFieldStart(fieldNames.getParameterErrors());
            for (ApiParameterError parameterError : parameterErrors) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField(fieldNames.getCode(), parameterError.getCode());
                jsonGenerator.writeStringField(fieldNames.getMessage(), parameterError.getMessage());
                jsonGenerator.writeStringField("parameter", parameterError.getParameter());
                jsonGenerator.writeObjectField("rejectedValue", parameterError.getRejectedValue());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }

        Map<String, Object> properties = errorResponse.getProperties();
        for (String property : properties.keySet()) {
            jsonGenerator.writeObjectField(property, properties.get(property));
        }

        jsonGenerator.writeEndObject();
    }
}
