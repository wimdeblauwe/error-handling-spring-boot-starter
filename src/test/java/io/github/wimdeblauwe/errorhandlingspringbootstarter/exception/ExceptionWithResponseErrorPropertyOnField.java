package io.github.wimdeblauwe.errorhandlingspringbootstarter.exception;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ResponseErrorProperty;

public class ExceptionWithResponseErrorPropertyOnField extends RuntimeException {
    @ResponseErrorProperty
    private final String myProperty;

    public ExceptionWithResponseErrorPropertyOnField(String message, String myProperty) {
        super(message);
        this.myProperty = myProperty;
    }

    public ExceptionWithResponseErrorPropertyOnField(String myProperty) {
        this.myProperty = myProperty;
    }

    public String getMyProperty() {
        return myProperty;
    }
}
