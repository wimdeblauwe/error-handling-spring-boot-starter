package io.github.wimdeblauwe.errorhandlingspringbootstarter.exception;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ResponseErrorProperty;

public class ExceptionWithResponseErrorPropertyOnMethodWithIncludeIfNull extends RuntimeException {
    private final String myProperty;

    public ExceptionWithResponseErrorPropertyOnMethodWithIncludeIfNull(String myProperty) {
        this.myProperty = myProperty;
    }

    @ResponseErrorProperty(includeIfNull = true)
    public String getMyProperty() {
        return myProperty;
    }
}
