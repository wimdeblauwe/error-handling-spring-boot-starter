package io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ResponseErrorCode;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Locale;

/**
 * This class contains the logic for getting the matching error code for the given {@link Throwable}.
 */
public class ErrorCodeMapper {

    private final ErrorHandlingProperties properties;

    public ErrorCodeMapper(ErrorHandlingProperties properties) {
        this.properties = properties;
    }

    public String getErrorCode(Throwable exception) {
        String exceptionClassName = exception.getClass().getName();
        if (properties.getCodes().containsKey(exceptionClassName)) {
            return properties.getCodes().get(exceptionClassName);
        }

        ResponseErrorCode errorCodeAnnotation = AnnotationUtils.getAnnotation(exception.getClass(), ResponseErrorCode.class);
        if (errorCodeAnnotation != null) {
            return errorCodeAnnotation.value();
        }

        switch (properties.getDefaultErrorCodeStrategy()) {
            case FULL_QUALIFIED_NAME:
                return exception.getClass().getName();
            case ALL_CAPS:
                return convertToAllCaps(exception.getClass().getSimpleName());
            default:
                throw new IllegalArgumentException("Unknown default error code strategy: " + properties.getDefaultErrorCodeStrategy());
        }
    }

    private String convertToAllCaps(String exceptionClassName) {
        String result = exceptionClassName.replaceFirst("Exception$", "");
        result = result.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase(Locale.ENGLISH);
        return result;
    }
}
