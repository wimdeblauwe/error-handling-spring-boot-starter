package io.github.wimdeblauwe.errorhandlingspringbootstarter.exception;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.exception.ExceptionWithResponseErrorPropertyOnField;

public class SubclassOfExceptionWithResponseErrorPropertyOnField extends ExceptionWithResponseErrorPropertyOnField {
    public SubclassOfExceptionWithResponseErrorPropertyOnField(String message,
                                                               String myProperty) {
        super(message, myProperty);
    }
}
