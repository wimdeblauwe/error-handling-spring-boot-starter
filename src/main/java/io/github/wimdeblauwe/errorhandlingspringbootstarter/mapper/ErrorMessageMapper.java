package io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;

/**
 * This class contains the logic for getting the matching error message for the given {@link Throwable}.
 */
public class ErrorMessageMapper {
    private final ErrorHandlingProperties properties;

    public ErrorMessageMapper(ErrorHandlingProperties properties) {
        this.properties = properties;
    }

    public String getErrorMessage(Throwable throwable) {
        String exceptionClassName = throwable.getClass().getName();
        if (properties.getMessages().containsKey(exceptionClassName)) {
            return properties.getMessages().get(exceptionClassName);
        }

        return throwable.getMessage();
    }
}
