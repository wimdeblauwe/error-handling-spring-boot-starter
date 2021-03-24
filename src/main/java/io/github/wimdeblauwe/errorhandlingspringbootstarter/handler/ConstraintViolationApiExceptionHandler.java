package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiFieldError;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiGlobalError;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import java.util.Set;

/**
 * {@link io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiExceptionHandler} for
 * {@link ConstraintViolationException}. This typically happens when there is validation
 * on Spring services that gets triggered.
 *
 * @see MethodArgumentNotValidApiExceptionHandler
 */
public class ConstraintViolationApiExceptionHandler extends AbstractApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintViolationApiExceptionHandler.class);

    public ConstraintViolationApiExceptionHandler(ErrorHandlingProperties properties) {
        super(properties);
    }

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof ConstraintViolationException;
    }

    @Override
    public ApiErrorResponse handle(Throwable exception) {

        ConstraintViolationException ex = (ConstraintViolationException) exception;
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_REQUEST,
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

        return response;
    }

    private ElementKind getElementKindOfLastNode(Path path) {
        ElementKind result = null;
        for (Path.Node node : path) {
            result = node.getKind();
        }

        return result;
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
