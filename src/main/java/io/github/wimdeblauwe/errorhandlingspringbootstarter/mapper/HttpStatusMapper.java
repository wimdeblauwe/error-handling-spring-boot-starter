package io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * This class contains the logic for getting the matching HTTP Status for the given {@link Throwable}.
 */
public class HttpStatusMapper {
    private final ErrorHandlingProperties properties;
    private final List<HttpResponseStatusFromExceptionMapper> httpResponseStatusFromExceptionMapperList;

    public HttpStatusMapper(ErrorHandlingProperties properties,
                            List<HttpResponseStatusFromExceptionMapper> httpResponseStatusFromExceptionMapperList) {
        this.properties = properties;
        this.httpResponseStatusFromExceptionMapperList = httpResponseStatusFromExceptionMapperList;
    }

    public HttpStatusCode getHttpStatus(Throwable exception) {
        return getHttpStatus(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public HttpStatusCode getHttpStatus(Throwable exception, HttpStatus defaultHttpStatus) {
        HttpStatusCode status = getHttpStatusFromPropertiesOrAnnotation(exception.getClass());
        if (status != null) {
            return status;
        }

        for (HttpResponseStatusFromExceptionMapper statusFromExceptionMapper : httpResponseStatusFromExceptionMapperList) {
            if( statusFromExceptionMapper.canExtractResponseStatus(exception)) {
                return statusFromExceptionMapper.getResponseStatus(exception);
            }
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
