package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import org.jspecify.annotations.Nullable;

public class ApiFieldError {
    private final String code;
    private final String property;
    private final String message;
    @Nullable
    private final Object rejectedValue;
    @Nullable
    private final String path;

    public ApiFieldError(String code, String property, String message, @Nullable Object rejectedValue, @Nullable String path) {
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

    @Nullable
    public Object getRejectedValue() {
        return rejectedValue;
    }

    @Nullable
    public String getPath() {
        return path;
    }
}
