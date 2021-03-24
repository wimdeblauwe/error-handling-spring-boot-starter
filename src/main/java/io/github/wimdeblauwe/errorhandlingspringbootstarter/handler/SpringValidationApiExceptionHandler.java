package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiFieldError;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiGlobalError;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import java.util.Iterator;
import java.util.Set;

public class SpringValidationApiExceptionHandler extends AbstractApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringValidationApiExceptionHandler.class);

    public SpringValidationApiExceptionHandler(ErrorHandlingProperties properties) {
        super(properties);
    }

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof MethodArgumentNotValidException
                || exception instanceof HttpMessageNotReadableException
                || exception instanceof ConstraintViolationException;
    }

    @Override
    public ApiErrorResponse handle(Throwable exception) {

        ApiErrorResponse response;
        if (exception instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) exception;
            response = new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                                            getErrorCode(exception),
                                            getMessage(ex));
            BindingResult bindingResult = ex.getBindingResult();
            if (bindingResult.hasFieldErrors()) {
                bindingResult.getFieldErrors().stream()
                             .map(fieldError -> new ApiFieldError(getCode(fieldError),
                                                                  fieldError.getField(),
                                                                  getMessage(fieldError),
                                                                  fieldError.getRejectedValue()))
                             .forEach(response::addFieldError);
            }

            if (bindingResult.hasGlobalErrors()) {
                bindingResult.getGlobalErrors().stream()
                             .map(globalError -> new ApiGlobalError(replaceCodeWithConfiguredOverrideIfPresent(globalError.getCode()),
                                                                    getMessage(globalError)))
                             .forEach(response::addGlobalError);
            }
        } else if (exception instanceof HttpMessageNotReadableException) {
            response = new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                                            replaceCodeWithConfiguredOverrideIfPresent(exception.getClass().getName()),
                                            exception.getMessage());

        } else if (exception instanceof ConstraintViolationException) {
            ConstraintViolationException ex = (ConstraintViolationException) exception;
            response = new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                                            getErrorCode(exception),
                                            getMessage(ex));
            Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
            violations.stream()
                      .map(constraintViolation -> {
                          ElementKind elementKind = getElementKindOfLastNode(constraintViolation.getPropertyPath());
                          if (elementKind == ElementKind.PROPERTY) {
                              return new ApiFieldError(getCode(constraintViolation),
                                                       constraintViolation.getPropertyPath().toString(),
                                                       getMessage(constraintViolation),
                                                       constraintViolation.getInvalidValue());
                          } else if (elementKind == ElementKind.BEAN) {
                              return new ApiGlobalError(getCode(constraintViolation),
                                                        getMessage(constraintViolation));
                          } else {
                              LOGGER.warn("Unable to convert constraint violation with element kind {}: {}", elementKind, constraintViolation);
                              return null;
                          }
                      })
                      .forEach(error -> {
                          if (error instanceof ApiFieldError) {
                              response.addFieldError((ApiFieldError) error);
                          } else if (error instanceof ApiGlobalError) {
                              response.addGlobalError((ApiGlobalError) error);
                          }
                      });

            // TODO test if getMessage works
            // TODO test if getCode works
        } else {
            throw new IllegalStateException("canHandle() and handle() methods are not in sync!");
        }

        return response;
    }

    private ElementKind getElementKindOfLastNode(Path path) {
        ElementKind result = null;
        for (Path.Node node : path) {
            result = node.getKind();
        }

        return result;
    }

    private String getCode(FieldError fieldError) {
        String fieldSpecificCode = fieldError.getField() + "." + fieldError.getCode();
        if (hasConfiguredOverrideForCode(fieldSpecificCode)) {
            return replaceCodeWithConfiguredOverrideIfPresent(fieldSpecificCode);
        }
        return replaceCodeWithConfiguredOverrideIfPresent(fieldError.getCode());
    }

    private String getMessage(FieldError fieldError) {
        String fieldSpecificKey = fieldError.getField() + "." + fieldError.getCode();
        if (hasConfiguredOverrideForMessage(fieldSpecificKey)) {
            return getOverrideMessage(fieldSpecificKey);
        }
        if (hasConfiguredOverrideForMessage(fieldError.getCode())) {
            return getOverrideMessage(fieldError.getCode());
        }
        return fieldError.getDefaultMessage();
    }

    private String getMessage(ObjectError objectError) {
        if (hasConfiguredOverrideForMessage(objectError.getCode())) {
            return getOverrideMessage(objectError.getCode());
        }
        return objectError.getDefaultMessage();
    }

    private String getMessage(MethodArgumentNotValidException exception) {
        return "Validation failed for object='" + exception.getBindingResult().getObjectName() + "'. Error count: " + exception.getBindingResult().getErrorCount();
    }

    private String getCode(ConstraintViolation<?> constraintViolation) {
        String code = constraintViolation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
        return replaceCodeWithConfiguredOverrideIfPresent(code);
    }

    private String getMessage(ConstraintViolation<?> constraintViolation) {
        String code = constraintViolation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
        if (hasConfiguredOverrideForMessage(code)) {
            return getOverrideMessage(code);
        }
        return constraintViolation.getMessage();
    }

    private String getMessage(ConstraintViolationException exception) {
        return "Validation failed. Error count: " + exception.getConstraintViolations().size();
    }
}
