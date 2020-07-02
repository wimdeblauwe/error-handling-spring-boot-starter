package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ControllerAdvice(annotations = RestController.class)
public class ErrorHandlingControllerAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandlingControllerAdvice.class);

    private final ErrorHandlingProperties properties;
    private final List<ApiExceptionHandler> handlers;
    private final FallbackApiExceptionHandler fallbackHandler;

    public ErrorHandlingControllerAdvice(ErrorHandlingProperties properties,
                                         List<ApiExceptionHandler> handlers,
                                         FallbackApiExceptionHandler fallbackHandler) {
        this.properties = properties;
        this.handlers = handlers;
        this.fallbackHandler = fallbackHandler;
        this.handlers.sort(AnnotationAwareOrderComparator.INSTANCE);

        LOGGER.info("Handlers: {}", this.handlers);
    }

    @ExceptionHandler
    public ResponseEntity<?> handleException(Throwable exception, WebRequest webRequest, Locale locale) {
        LOGGER.debug("webRequest: {}", webRequest);
        LOGGER.debug("locale: {}", locale);
        logException(exception);

        ApiErrorResponse errorResponse = null;
        for (ApiExceptionHandler handler : handlers) {
            if (handler.canHandle(exception)) {
                errorResponse = handler.handle(exception);
                break;
            }
        }

        if (errorResponse == null) {
            errorResponse = fallbackHandler.handle(exception);
        }

        return ResponseEntity.status(errorResponse.getHttpStatus())
                             .body(errorResponse);




        /*HttpStatus statusCode = getHttpStatus(exception);
        String errorCode = getErrorCode(exception);

        ApiErrorResponse response = new ApiErrorResponse(statusCode, errorCode, getErrorMessage(exception));
        response.addErrorProperties(getMethodResponseErrorProperties(exception));
        response.addErrorProperties(getFieldResponseErrorProperties(exception));

        if (exception instanceof MethodArgumentNotValidException) {
            statusCode = HttpStatus.BAD_REQUEST;
            BindingResult bindingResult = ((MethodArgumentNotValidException) exception).getBindingResult();
            if (bindingResult.hasFieldErrors()) {
                List<FieldError> allErrors = bindingResult.getFieldErrors();
                List<Map<String, String>> fieldErrors = new ArrayList<>();
                for (FieldError fieldError : allErrors) {
                    Map<String, String> mapForError = new HashMap<>();
                    mapForError.put("code", replaceCodeWithConfiguredOverrideIfPresent(fieldError.getCode()));
                    mapForError.put("property", fieldError.getField());
                    mapForError.put("message", fieldError.getDefaultMessage());
                    mapForError.put("rejectedValue", String.valueOf(fieldError.getRejectedValue()));
                    fieldErrors.add(mapForError);
                }
                body.put("fieldErrors", fieldErrors);
            }

            if (bindingResult.hasGlobalErrors()) {
                List<ObjectError> globalErrors = bindingResult.getGlobalErrors();
                List<Map<String, String>> globalErrorsList = new ArrayList<>();
                for (ObjectError objectError : globalErrors) {
                    Map<String, String> mapForError = new HashMap<>();
                    mapForError.put("code", replaceCodeWithConfiguredOverrideIfPresent(objectError.getCode()));
                    mapForError.put("message", objectError.getDefaultMessage());
                    globalErrorsList.add(mapForError);
                }
                body.put("globalErrors", globalErrorsList);
            }
        }

        return ResponseEntity.status(statusCode)
                             .body(body);*/
    }

/*
    private void addResponseErrorProperties(Throwable exception, Map<String, Object> body) {
        addMethodResponseErrorProperties(exception);
        addFieldResponseErrorProperties(exception);
    }
*/

    private Map<String, String> getFieldResponseErrorProperties(Throwable exception) {
        Map<String, String> result = new HashMap<>();
        for (Field field : exception.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ResponseErrorProperty.class)) {
                try {
                    field.setAccessible(true);
                    result.put(field.getName(), String.valueOf(field.get(exception)));
                } catch (IllegalAccessException e) {
                    LOGGER.error(String.format("Unable to use field result of field %s.%s", exception.getClass().getName(), field.getName()));
                }
            }
        }
        return result;
/*
        Stream.of(exception.getClass().getDeclaredFields())
              .filter(field -> field.isAnnotationPresent(ResponseErrorProperty.class))
              .forEachOrdered(field -> {
                  try {
                      field.setAccessible(true);
                      body.put(field.getName(), String.valueOf(field.get(exception)));
                  } catch (IllegalAccessException e) {
                      LOGGER.error(String.format("Unable to use field result of field %s.%s", exception.getClass().getName(), field.getName()));
                  }
              });
*/
    }

    private Map<String, String> getMethodResponseErrorProperties(Throwable exception) {
        Map<String, String> result = new HashMap<>();
        for (Method method : exception.getClass().getMethods()) {
            if (method.isAnnotationPresent(ResponseErrorProperty.class)
                    && method.getReturnType() != Void.TYPE
                    && method.getParameterCount() == 0) {
                try {
                    method.setAccessible(true);
                    result.put(method.getName(), String.valueOf(method.invoke(exception)));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    LOGGER.error(String.format("Unable to use method result of method %s.%s", exception.getClass().getName(), method.getName()));
                }
            }
        }
        return result;
/*
        Stream.of(exception.getClass().getMethods())
              .filter(method -> method.isAnnotationPresent(ResponseErrorProperty.class)
                      && method.getReturnType() != Void.TYPE
                      && method.getParameterCount() == 0)
              .forEachOrdered(method -> {
                  try {
                      method.setAccessible(true);
                      body.put(method.getName(), String.valueOf(method.invoke(exception)));
                  } catch (IllegalAccessException | InvocationTargetException e) {
                      LOGGER.error(String.format("Unable to use method result of method %s.%s", exception.getClass().getName(), method.getName()));
                  }
              });
*/
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
            code = exception.getClass().getSimpleName();
        }

        return replaceCodeWithConfiguredOverrideIfPresent(code);
    }

    private String replaceCodeWithConfiguredOverrideIfPresent(String code) {
        return properties.getCodes().getOrDefault(code, code);
    }

    private String getErrorMessage(Throwable exception) {
        return exception.getMessage();
    }

    private void logException(Throwable exception) {
        switch (properties.getExceptionLogging()) {
            case WITH_STACKTRACE:
                LOGGER.error(exception.getMessage(), exception);
                break;
            case MESSAGE_ONLY:
                LOGGER.error(exception.getMessage());
                break;
        }
    }
}
