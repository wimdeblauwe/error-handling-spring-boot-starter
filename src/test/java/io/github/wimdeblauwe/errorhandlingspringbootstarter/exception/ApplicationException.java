package io.github.wimdeblauwe.errorhandlingspringbootstarter.exception;

public class ApplicationException extends RuntimeException {

    public ApplicationException(String message) {
        super(message);
    }

}
