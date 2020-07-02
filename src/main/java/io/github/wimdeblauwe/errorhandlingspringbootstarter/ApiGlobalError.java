package io.github.wimdeblauwe.errorhandlingspringbootstarter;

public class ApiGlobalError {
    private final String code;
    private final String message;

    public ApiGlobalError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
