package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.*;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

/**
 * {@link io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiExceptionHandler} for
 * {@link ConstraintViolationException}. This typically happens when there is validation
 * on Spring services that gets triggered.
 *
 * @see MethodArgumentNotValidApiExceptionHandler
 */
public class ConstraintViolationApiExceptionHandler extends AbstractApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintViolationApiExceptionHandler.class);

    public ConstraintViolationApiExceptionHandler(ErrorHandlingProperties properties,
                                                  HttpStatusMapper httpStatusMapper,
                                                  ErrorCodeMapper errorCodeMapper,
                                                  ErrorMessageMapper errorMessageMapper) {
        super(properties, httpStatusMapper, errorCodeMapper, errorMessageMapper);
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
                      Optional<Path.Node> leafNode = getLeafNode(constraintViolation.getPropertyPath());
                      if (leafNode.isPresent()) {
                          Path.Node node = leafNode.get();
                          ElementKind elementKind = node.getKind();
                          if (elementKind == ElementKind.PROPERTY) {
                              return new ApiFieldError(getCode(constraintViolation),
                                                       node.toString(),
                                                       getMessage(constraintViolation),
                                                       constraintViolation.getInvalidValue());
                          } else if (elementKind == ElementKind.BEAN) {
                              return new ApiGlobalError(getCode(constraintViolation),
                                                        getMessage(constraintViolation));
                          } else if (elementKind == ElementKind.PARAMETER) {
                              return new ApiParameterError(getCode(constraintViolation),
                                                           node.toString(),
                                                           getMessage(constraintViolation),
                                                           constraintViolation.getInvalidValue());
                          } else {
                              LOGGER.warn("Unable to convert constraint violation with element kind {}: {}", elementKind, constraintViolation);
                              return null;
                          }
                      } else {
                          LOGGER.warn("Unable to convert constraint violation: {}", constraintViolation);
                          return null;
                      }
                  })
                  .forEach(error -> {
                      if (error instanceof ApiFieldError) {
                          response.addFieldError((ApiFieldError) error);
                      } else if (error instanceof ApiGlobalError) {
                          response.addGlobalError((ApiGlobalError) error);
                      } else if (error instanceof ApiParameterError) {
                          response.addParameterError((ApiParameterError) error);
                      }
                  });

        return response;
    }

    private Optional<Path.Node> getLeafNode(Path path) {
        return StreamSupport.stream(path.spliterator(), false).reduce((a, b) -> b);
    }

    private String getCode(ConstraintViolation<?> constraintViolation) {
        String code = constraintViolation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
        String fieldSpecificCode = constraintViolation.getPropertyPath().toString() + "." + code;
        return errorCodeMapper.getErrorCode(fieldSpecificCode, code);
    }

    private String getMessage(ConstraintViolation<?> constraintViolation) {
        String code = constraintViolation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
        String fieldSpecificCode = constraintViolation.getPropertyPath().toString() + "." + code;
        return errorMessageMapper.getErrorMessage(fieldSpecificCode, code, constraintViolation.getMessage());
    }

    private String getMessage(ConstraintViolationException exception) {
        return "Validation failed. Error count: " + exception.getConstraintViolations().size();
    }
}
