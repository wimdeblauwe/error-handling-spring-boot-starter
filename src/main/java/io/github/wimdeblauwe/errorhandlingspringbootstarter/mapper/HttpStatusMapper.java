package io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * This class contains the logic for getting the matching HTTP Status for the given {@link Throwable}.
 */
public class HttpStatusMapper {
    private final ErrorHandlingProperties properties;

    public HttpStatusMapper(ErrorHandlingProperties properties) {
        this.properties = properties;
    }

    public HttpStatus getHttpStatus(Throwable exception) {
        return getHttpStatus(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public HttpStatus getHttpStatus(Throwable exception, HttpStatus defaultHttpStatus) {
        // Find the first existing HttpStatus override throw the class hierarchy
        HttpStatus status = getHttpStatus(exception.getClass());
        if (status != null) {
            return status;
        }

        // If not, found check if the exception includes the HttpStatus
        if (exception instanceof ResponseStatusException) {
            return ((ResponseStatusException) exception).getStatus();
        }

        // If not, return default
        return defaultHttpStatus;
    }

    private HttpStatus getHttpStatus(Class<?> exceptionClass) {
        if (exceptionClass == null) {
            return null;
        }
        String exceptionClassName = exceptionClass.getName();
        if (properties.getHttpStatuses().containsKey(exceptionClassName)) {
            return properties.getHttpStatuses().get(exceptionClassName);
        }

        ResponseStatus responseStatus = AnnotationUtils.getAnnotation(exceptionClass, ResponseStatus.class);
        if (responseStatus != null) {
            return responseStatus.value();
        }

        return getHttpStatus(exceptionClass.getSuperclass());
    }

}
