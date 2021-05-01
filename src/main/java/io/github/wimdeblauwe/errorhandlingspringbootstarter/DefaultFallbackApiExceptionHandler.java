package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

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

    private final HttpStatusMapper httpStatusMapper;
    private final ErrorCodeMapper errorCodeMapper;
    private final ErrorMessageMapper errorMessageMapper;

    public DefaultFallbackApiExceptionHandler(HttpStatusMapper httpStatusMapper,
                                              ErrorCodeMapper errorCodeMapper,
                                              ErrorMessageMapper errorMessageMapper) {
        this.httpStatusMapper = httpStatusMapper;
        this.errorCodeMapper = errorCodeMapper;
        this.errorMessageMapper = errorMessageMapper;
    }

    @Override
    public ApiErrorResponse handle(Throwable exception) {
        HttpStatus statusCode = httpStatusMapper.getHttpStatus(exception);
        String errorCode = errorCodeMapper.getErrorCode(exception);
        String errorMessage = errorMessageMapper.getErrorMessage(exception);

        ApiErrorResponse response = new ApiErrorResponse(statusCode, errorCode, errorMessage);
        response.addErrorProperties(getMethodResponseErrorProperties(exception));
        response.addErrorProperties(getFieldResponseErrorProperties(exception));

        return response;
    }

    private Map<String, Object> getFieldResponseErrorProperties(Throwable exception) {
        Map<String, Object> result = new HashMap<>();
        for (Field field : exception.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ResponseErrorProperty.class)) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(exception);
                    if (value != null || field.getAnnotation(ResponseErrorProperty.class).includeIfNull()) {
                        result.put(getPropertyName(field), value);
                    }
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

                    Object value = method.invoke(exception);
                    if (value != null || method.getAnnotation(ResponseErrorProperty.class).includeIfNull()) {
                        result.put(getPropertyName(exceptionClass, method),
                                   value);
                    }
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
}
