package io.github.wimdeblauwe.errorhandlingspringbootstarter.exception;

public class SubclassOfExceptionWithResponseErrorPropertyOnMethod extends ExceptionWithResponseErrorPropertyOnMethod {
    public SubclassOfExceptionWithResponseErrorPropertyOnMethod(String message,
                                                                String myProperty) {
        super(message, myProperty);
    }
}
