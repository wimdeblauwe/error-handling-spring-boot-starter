package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.GraphQlErrorMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.HashMap;
import java.util.Map;

public class DefaultFallbackGraphqlExceptionHandler implements FallbackGraphqlExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFallbackGraphqlExceptionHandler.class);

    private final HttpStatusMapper httpStatusMapper;
    private final ErrorCodeMapper errorCodeMapper;
    private final ErrorMessageMapper errorMessageMapper;

    private final GraphQlErrorMapper graphQlErrorMapper;

    public DefaultFallbackGraphqlExceptionHandler(HttpStatusMapper httpStatusMapper,
                                                  ErrorCodeMapper errorCodeMapper,
                                                  ErrorMessageMapper errorMessageMapper,
                                                  GraphQlErrorMapper graphQlErrorMapper) {
        this.httpStatusMapper = httpStatusMapper;
        this.errorCodeMapper = errorCodeMapper;
        this.errorMessageMapper = errorMessageMapper;
        this.graphQlErrorMapper = graphQlErrorMapper;
    }

    @Override
    public GraphQLError handle(Throwable exception) {
        HttpStatusCode statusCode = httpStatusMapper.getHttpStatus(exception);
        String errorCode = errorCodeMapper.getErrorCode(exception);
        String errorMessage = errorMessageMapper.getErrorMessage(exception);
        ErrorClassification errorType = graphQlErrorMapper.httpStatusToErrorClassification((HttpStatus) statusCode);
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("code", errorCode);

        GraphqlErrorBuilder builder = GraphqlErrorBuilder
                .newError()
                .errorType(errorType)
                .message(errorMessage != null ? errorMessage : "Unknown error")
                .extensions(extensions);

        return builder.build();
    }
}
