package io.github.wimdeblauwe.errorhandlingspringbootstarter.exception;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ResponseErrorProperty;

public class ExceptionWithResponseErrorPropertyOnMethod extends RuntimeException {
    private final String myProperty;

    public ExceptionWithResponseErrorPropertyOnMethod(String message, String myProperty) {
        super(message);
        this.myProperty = myProperty;
    }

    public ExceptionWithResponseErrorPropertyOnMethod(String myProperty) {
        this.myProperty = myProperty;
    }

    @ResponseErrorProperty
    public String getMyProperty() {
        return myProperty;
    }
}
