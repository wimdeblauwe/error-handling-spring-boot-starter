package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.*;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.GraphQlErrorMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GqlConstraintViolationExceptionHandler extends AbstractGraphqlExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GqlConstraintViolationExceptionHandler.class);

    private final ErrorHandlingProperties properties;

    public GqlConstraintViolationExceptionHandler(ErrorHandlingProperties properties,
                                                  HttpStatusMapper httpStatusMapper,
                                                  ErrorCodeMapper errorCodeMapper,
                                                  ErrorMessageMapper errorMessageMapper,
                                                  GraphQlErrorMapper graphQlErrorMapper) {
        super(httpStatusMapper, errorCodeMapper, errorMessageMapper, graphQlErrorMapper);
        this.properties = properties;
    }

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof ConstraintViolationException;
    }

    @Override
    public GraphQLError handle(Throwable exception) {
        ConstraintViolationException ex = (ConstraintViolationException) exception;
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                                                         getErrorCode(exception),
                                                         getMessage(ex));
        Map<String, Object> extensions = new HashMap<>();
        violations.stream()
                  // sort violations to ensure deterministic order
                  .sorted(Comparator.comparing(constraintViolation -> constraintViolation.getPropertyPath().toString()))
                  .map(constraintViolation -> {
                      Optional<Path.Node> leafNode = getLeafNode(constraintViolation.getPropertyPath());
                      if (leafNode.isPresent()) {
                          Path.Node node = leafNode.get();
                          ElementKind elementKind = node.getKind();
                          if (elementKind == ElementKind.PROPERTY) {
                              return new ApiFieldError(getCode(constraintViolation),
                                                       node.toString(),
                                                       getMessage(constraintViolation),
                                                       constraintViolation.getInvalidValue(),
                                                       getPath(constraintViolation));
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
                      if (error instanceof ApiFieldError apiFieldError) {
                          response.addFieldError(apiFieldError);
                      } else if (error instanceof ApiGlobalError apiGlobalError) {
                          response.addGlobalError(apiGlobalError);
                      } else if (error instanceof ApiParameterError apiParameterError) {
                          response.addParameterError(apiParameterError);
                      }
                  });

        extensions.put("errorDetails", response);
        extensions.put("code", response.getCode());

        return GraphqlErrorBuilder
                .newError()
                .errorType(graphQlErrorMapper.httpStatusToErrorClassification((HttpStatus) response.getHttpStatus()))
                .message(response.getMessage())
                .extensions(extensions)
                .build();
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
