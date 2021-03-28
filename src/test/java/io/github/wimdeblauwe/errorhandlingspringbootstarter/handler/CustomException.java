package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

public class CustomException extends RuntimeException {
    public CustomException(String message,
                           Throwable cause) {
        super(message, cause);
    }
}
