package io.github.wimdeblauwe.errorhandlingspringbootstarter.exception;

public class SubclassOfApplicationException extends ApplicationException {

    public SubclassOfApplicationException(String message) {
        super(message);
    }
    
}
