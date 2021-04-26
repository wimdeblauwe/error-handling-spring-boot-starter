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
         // Find the first existing error code override throw the class hierarchy
         String code = getErrorCode(exception.getClass());
        if (code != null) {
            return code;
        }
        // If not found return default using configured CodeStrategy
        switch (properties.getDefaultErrorCodeStrategy()) {
            case FULL_QUALIFIED_NAME:
                return exception.getClass().getName();
            case ALL_CAPS:
                return convertToAllCaps(exception.getClass().getSimpleName());
            default:
                throw new IllegalArgumentException("Unknown default error code strategy: " + properties.getDefaultErrorCodeStrategy());
        }
    }

    private String getErrorCode(Class<?> exceptionClass) {
        if (exceptionClass == null) {
            return null;
        }
        // Check if a property overriding exisits
        String exceptionClassName = exceptionClass.getName();
        if (properties.getCodes().containsKey(exceptionClassName)) {
            return properties.getCodes().get(exceptionClassName);
        }
        // If not, check if an annotation exists
        ResponseErrorCode errorCodeAnnotation = AnnotationUtils.getAnnotation(exceptionClass, ResponseErrorCode.class);
        if (errorCodeAnnotation != null) {
            return errorCodeAnnotation.value();
        }
        // If not, check ancestor
        return getErrorCode(exceptionClass.getSuperclass());
    }

    public String getErrorCode(String fieldSpecificErrorCode, String errorCode) {
        if (properties.getCodes().containsKey(fieldSpecificErrorCode)) {
            return properties.getCodes().get(fieldSpecificErrorCode);
        }

        return getErrorCode(errorCode);
    }

    public String getErrorCode(String errorCode) {
        if (properties.getCodes().containsKey(errorCode)) {
            return properties.getCodes().get(errorCode);
        }

        return errorCode;
    }

    private String convertToAllCaps(String exceptionClassName) {
        String result = exceptionClassName.replaceFirst("Exception$", "");
        result = result.replaceAll("([a-z])([A-Z]+)", "$1_$2").toUpperCase(Locale.ENGLISH);
        return result;
    }
}
