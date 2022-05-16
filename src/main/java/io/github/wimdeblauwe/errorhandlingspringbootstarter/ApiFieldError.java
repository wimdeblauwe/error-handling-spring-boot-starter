package io.github.wimdeblauwe.errorhandlingspringbootstarter;

public class ApiFieldError {
    private final String code;
    private final String property;
    private final String message;
    private final Object rejectedValue;
    private final String path;

    public ApiFieldError(String code, String property, String message, Object rejectedValue, String path) {
        this.code = code;
        this.property = property;
        this.message = message;
        this.rejectedValue = rejectedValue;
        this.path = path;
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

    public String getPath() {
        return path;
    }
}
