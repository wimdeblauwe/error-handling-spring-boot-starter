package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatusCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiErrorResponse {
    @Nullable
    private final HttpStatusCode httpStatus;
    private final String code;
    @Nullable
    private final String message;
    private final Map<String, @Nullable Object> properties;
    private final List<ApiFieldError> fieldErrors;
    private final List<ApiGlobalError> globalErrors;
    private final List<ApiParameterError> parameterErrors;

    public ApiErrorResponse(@Nullable HttpStatusCode httpStatus, String code, @Nullable String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.properties = new HashMap<>();
        this.fieldErrors = new ArrayList<>();
        this.globalErrors = new ArrayList<>();
        this.parameterErrors = new ArrayList<>();
    }

    @Nullable
    @JsonIgnore
    public HttpStatusCode getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    @JsonAnyGetter
    public Map<String, @Nullable Object> getProperties() {
        return properties;
    }

    public List<ApiFieldError> getFieldErrors() {
        return fieldErrors;
    }

    public List<ApiGlobalError> getGlobalErrors() {
        return globalErrors;
    }

    public List<ApiParameterError> getParameterErrors() {
        return parameterErrors;
    }

    public void addErrorProperties(Map<String, @Nullable Object> errorProperties) {
        properties.putAll(errorProperties);
    }

    public void addErrorProperty(String propertyName, @Nullable Object propertyValue) {
        properties.put(propertyName, propertyValue);
    }

    public void addFieldError(ApiFieldError fieldError) {
        fieldErrors.add(fieldError);
    }

    public void addGlobalError(ApiGlobalError globalError) {
        globalErrors.add(globalError);
    }

    public void addParameterError(ApiParameterError parameterError) {
        parameterErrors.add(parameterError);
    }
}
