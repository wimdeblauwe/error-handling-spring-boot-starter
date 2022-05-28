package io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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

    public HttpStatusCode getHttpStatus(Throwable exception) {
        return getHttpStatus(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public HttpStatusCode getHttpStatus(Throwable exception, HttpStatus defaultHttpStatus) {
        HttpStatusCode status = getHttpStatusFromPropertiesOrAnnotation(exception.getClass());
        if (status != null) {
            return status;
        }

        if (exception instanceof ResponseStatusException) {
            return ((ResponseStatusException) exception).getStatusCode();
        }

        return defaultHttpStatus;
    }

    private HttpStatusCode getHttpStatusFromPropertiesOrAnnotation(Class<?> exceptionClass) {
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

        if (properties.isSearchSuperClassHierarchy()) {
            return getHttpStatusFromPropertiesOrAnnotation(exceptionClass.getSuperclass());
        } else {
            return null;
        }
    }

}
