package io.github.wimdeblauwe.errorhandlingspringbootstarter.exception;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ResponseErrorProperty;

public class ExceptionWithResponseErrorPropertyOnFieldWithIncludeIfNull extends RuntimeException {
    @ResponseErrorProperty(includeIfNull = true)
    private final String myProperty;

    public ExceptionWithResponseErrorPropertyOnFieldWithIncludeIfNull(String myProperty) {
        this.myProperty = myProperty;
    }

    public String getMyProperty() {
        return myProperty;
    }
}
