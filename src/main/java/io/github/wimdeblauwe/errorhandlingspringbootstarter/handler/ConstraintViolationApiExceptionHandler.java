package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiFieldError;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiGlobalError;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiParameterError;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * {@link io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiExceptionHandler} for
 * {@link ConstraintViolationException}. This typically happens when there is validation
 * on Spring services that gets triggered.
 *
 * @see BindApiExceptionHandler
 */
public class ConstraintViolationApiExceptionHandler extends AbstractApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintViolationApiExceptionHandler.class);
    private final ErrorHandlingProperties properties;

    public ConstraintViolationApiExceptionHandler(ErrorHandlingProperties properties,
                                                  HttpStatusMapper httpStatusMapper,
                                                  ErrorCodeMapper errorCodeMapper,
                                                  ErrorMessageMapper errorMessageMapper) {
        super(httpStatusMapper, errorCodeMapper, errorMessageMapper);
        this.properties = properties;
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
                  // sort violations to ensure deterministic order
                  .sorted(Comparator.comparing(constraintViolation -> constraintViolation.getPropertyPath().toString()))
                  .forEach(constraintViolation -> {
                      Optional<Path.Node> leafNode = getLeafNode(constraintViolation.getPropertyPath());
                      if (leafNode.isPresent()) {
                          Path.Node node = leafNode.get();
                          ElementKind elementKind = node.getKind();
                          if (elementKind == ElementKind.PROPERTY) {
                              var fieldError = new ApiFieldError(getCode(constraintViolation),
                                                       node.toString(),
                                                       getMessage(constraintViolation),
                                                       constraintViolation.getInvalidValue(),
                                                       getPath(constraintViolation));
                              response.addFieldError(fieldError);
                          } else if (elementKind == ElementKind.BEAN) {
                              var globalError = new ApiGlobalError(getCode(constraintViolation),
                                                        getMessage(constraintViolation));
                              response.addGlobalError(globalError);
                          } else if (elementKind == ElementKind.PARAMETER) {
                              var parameterError = new ApiParameterError(getCode(constraintViolation),
                                                           node.toString(),
                                                           getMessage(constraintViolation),
                                                           constraintViolation.getInvalidValue());
                              response.addParameterError(parameterError);
                          } else {
                              LOGGER.warn("Unable to convert constraint violation with element kind {}: {}", elementKind, constraintViolation);
                          }
                      } else {
                          LOGGER.warn("Unable to convert constraint violation: {}", constraintViolation);
                      }
                  });
        return response;
    }

    private Optional<Path.Node> getLeafNode(Path path) {
        return StreamSupport.stream(path.spliterator(), false).reduce((a, b) -> b);
    }

    private String getPath(ConstraintViolation<?> constraintViolation) {
        if (!properties.isAddPathToError()) {
            return null;
        }

        return getPathWithoutPrefix(constraintViolation.getPropertyPath());
    }

    /**
     * This method will truncate the first 2 parts of the full property path so the
     * method name and argument name are not visible in the returned path.
     *
     * @param path the full property path of the constraint violation
     * @return The truncated property path
     */
    private String getPathWithoutPrefix(Path path) {
        String collect = StreamSupport.stream(path.spliterator(), false)
                                      .limit(2)
                                      .map(Path.Node::getName)
                                      .collect(Collectors.joining("."));
        String substring = path.toString().substring(collect.length());
        return substring.startsWith(".") ? substring.substring(1) : substring;
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
