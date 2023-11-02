package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.GqlErrorType;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.GraphQlErrorMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashMap;
import java.util.Map;

public class GqlAccessDeniedExceptionHandler extends AbstractGraphqlExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GqlAccessDeniedExceptionHandler.class);

    private final ErrorHandlingProperties properties;

    public GqlAccessDeniedExceptionHandler(ErrorHandlingProperties errorHandlingProperties,
                                           HttpStatusMapper httpStatusMapper,
                                           ErrorCodeMapper errorCodeMapper,
                                           ErrorMessageMapper errorMessageMapper,
                                           GraphQlErrorMapper graphQlErrorMapper) {
        super(httpStatusMapper, errorCodeMapper, errorMessageMapper, graphQlErrorMapper);
        this.properties = errorHandlingProperties;
    }

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof AccessDeniedException || exception instanceof java.nio.file.AccessDeniedException;
    }

    @Override
    public GraphQLError handle(Throwable exception) {
        String errorCode = errorCodeMapper.getErrorCode(exception);
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("code", errorCode);

        return GraphqlErrorBuilder
                .newError()
                .errorType(GqlErrorType.FORBIDDEN)
                .message(super.getErrorMessage(exception))
                .extensions(extensions)
                .build();
    }
}
