package io.github.wimdeblauwe.errorhandlingspringbootstarter;

public class ApiFieldError {
    private final String code;
    private final String property;
    private final String message;
    private final Object rejectedValue;

    public ApiFieldError(String code, String property, String message, Object rejectedValue) {
        this.code = code;
        this.property = property;
        this.message = message;
        this.rejectedValue = rejectedValue;
    }

    public String getCode() {
        return code;
    }

    public String getProperty() {
        return property;
    }

    public String getMessage() {
        return message;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }
}
