package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

public class HttpStatusMapper {
    private final ErrorHandlingProperties properties;

    public HttpStatusMapper(ErrorHandlingProperties properties) {
        this.properties = properties;
    }

    public HttpStatus getHttpStatus(Throwable exception) {
        String exceptionClassName = exception.getClass().getName();
        if (properties.getHttpStatuses().containsKey(exceptionClassName)) {
            return properties.getHttpStatuses().get(exceptionClassName);
        }

        ResponseStatus responseStatus = AnnotationUtils.getAnnotation(exception.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            return responseStatus.value();
        }

        if (exception instanceof ResponseStatusException) {
            return ((ResponseStatusException) exception).getStatus();
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
