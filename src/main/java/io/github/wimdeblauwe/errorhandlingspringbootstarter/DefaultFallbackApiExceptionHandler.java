package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


public class DefaultFallbackApiExceptionHandler implements FallbackApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFallbackApiExceptionHandler.class);

    private final ErrorHandlingProperties properties;

    public DefaultFallbackApiExceptionHandler(ErrorHandlingProperties properties) {
        this.properties = properties;
    }

    @Override
    public ApiErrorResponse handle(Throwable exception) {
        HttpStatus statusCode = getHttpStatus(exception);
        String errorCode = getErrorCode(exception);

        ApiErrorResponse response = new ApiErrorResponse(statusCode, errorCode, getErrorMessage(exception));
        response.addErrorProperties(getMethodResponseErrorProperties(exception));
        response.addErrorProperties(getFieldResponseErrorProperties(exception));

        return response;
    }

    private String getErrorMessage(Throwable exception) {
        return exception.getMessage();
    }

    private Map<String, Object> getFieldResponseErrorProperties(Throwable exception) {
        Map<String, Object> result = new HashMap<>();
        for (Field field : exception.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ResponseErrorProperty.class)) {
                try {
                    field.setAccessible(true);
                    result.put(getPropertyName(field), field.get(exception));
                } catch (IllegalAccessException e) {
                    LOGGER.error(String.format("Unable to use field result of field %s.%s", exception.getClass().getName(), field.getName()));
                }
            }
        }
        return result;
    }

    private Map<String, Object> getMethodResponseErrorProperties(Throwable exception) {
        Map<String, Object> result = new HashMap<>();
        Class<? extends Throwable> exceptionClass = exception.getClass();
        for (Method method : exceptionClass.getMethods()) {
            if (method.isAnnotationPresent(ResponseErrorProperty.class)
                    && method.getReturnType() != Void.TYPE
                    && method.getParameterCount() == 0) {
                try {
                    method.setAccessible(true);

                    result.put(getPropertyName(exceptionClass, method),
                               method.invoke(exception));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    LOGGER.error(String.format("Unable to use method result of method %s.%s", exceptionClass.getName(), method.getName()));
                }
            }
        }
        return result;
    }

    private String getPropertyName(Field field) {
        ResponseErrorProperty annotation = AnnotationUtils.getAnnotation(field, ResponseErrorProperty.class);
        assert annotation != null;
        if (!StringUtils.isEmpty(annotation.value())) {
            return annotation.value();
        }

        return field.getName();
    }

    private String getPropertyName(Class<? extends Throwable> exceptionClass, Method method) {
        ResponseErrorProperty annotation = AnnotationUtils.getAnnotation(method, ResponseErrorProperty.class);
        assert annotation != null;
        if (!StringUtils.isEmpty(annotation.value())) {
            return annotation.value();
        }

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(exceptionClass);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (propertyDescriptor.getReadMethod().equals(method)) {
                    return propertyDescriptor.getName();
                }
            }
        } catch (IntrospectionException e) {
            //ignore
        }

        return method.getName();
    }

    private HttpStatus getHttpStatus(Throwable exception) {
        ResponseStatus responseStatus = AnnotationUtils.getAnnotation(exception.getClass(), ResponseStatus.class);
        return responseStatus != null ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String getErrorCode(Throwable exception) {
        ResponseErrorCode errorCodeAnnotation = AnnotationUtils.getAnnotation(exception.getClass(), ResponseErrorCode.class);
        String code;
        if (errorCodeAnnotation != null) {
            code = errorCodeAnnotation.value();
        } else {
            code = exception.getClass().getName();
        }

        return replaceCodeWithConfiguredOverrideIfPresent(code);
    }

    private String replaceCodeWithConfiguredOverrideIfPresent(String code) {
        return properties.getCodes().getOrDefault(code, code);
    }

}
