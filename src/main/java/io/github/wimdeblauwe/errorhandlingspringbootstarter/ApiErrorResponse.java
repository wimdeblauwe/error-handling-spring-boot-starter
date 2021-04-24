package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiErrorResponse {
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    private final Map<String, Object> properties;
    private final List<ApiFieldError> fieldErrors;
    private final List<ApiGlobalError> globalErrors;
    private final List<ApiParameterError> parameterErrors;

    public ApiErrorResponse(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
        this.properties = new HashMap<>();
        this.fieldErrors = new ArrayList<>();
        this.globalErrors = new ArrayList<>();
        this.parameterErrors = new ArrayList<>();
    }

    @JsonIgnore
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties() {
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

    public void addErrorProperties(Map<String, Object> errorProperties) {
        properties.putAll(errorProperties);
    }

    public void addErrorProperty(String propertyName, Object propertyValue) {
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
