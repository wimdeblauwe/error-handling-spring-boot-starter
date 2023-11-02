package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.handler.*;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.GraphQlErrorMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

public abstract class AbstractErrorHandlingConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public LoggingService loggingService(ErrorHandlingProperties properties,
                                         GraphQlErrorMapper graphQlErrorMapper) {
        return new LoggingService(properties, graphQlErrorMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpStatusMapper httpStatusMapper(ErrorHandlingProperties properties) {
        return new HttpStatusMapper(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorCodeMapper errorCodeMapper(ErrorHandlingProperties properties) {
        return new ErrorCodeMapper(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public GraphQlErrorMapper graphQlErrorMapper(ErrorHandlingProperties properties) {
        return new GraphQlErrorMapper(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorMessageMapper errorMessageMapper(ErrorHandlingProperties properties) {
        return new ErrorMessageMapper(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public FallbackApiExceptionHandler defaultHandler(HttpStatusMapper httpStatusMapper,
                                                      ErrorCodeMapper errorCodeMapper,
                                                      ErrorMessageMapper errorMessageMapper) {
        return new DefaultFallbackApiExceptionHandler(httpStatusMapper,
                                                      errorCodeMapper,
                                                      errorMessageMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public TypeMismatchApiExceptionHandler typeMismatchApiExceptionHandler(ErrorHandlingProperties properties,
                                                                           HttpStatusMapper httpStatusMapper,
                                                                           ErrorCodeMapper errorCodeMapper,
                                                                           ErrorMessageMapper errorMessageMapper) {
        return new TypeMismatchApiExceptionHandler(properties, httpStatusMapper, errorCodeMapper, errorMessageMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpMessageNotReadableApiExceptionHandler httpMessageNotReadableApiExceptionHandler(ErrorHandlingProperties properties,
                                                                                               HttpStatusMapper httpStatusMapper,
                                                                                               ErrorCodeMapper errorCodeMapper,
                                                                                               ErrorMessageMapper errorMessageMapper) {
        return new HttpMessageNotReadableApiExceptionHandler(properties, httpStatusMapper, errorCodeMapper, errorMessageMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public BindApiExceptionHandler bindApiExceptionHandler(ErrorHandlingProperties properties,
                                                           HttpStatusMapper httpStatusMapper,
                                                           ErrorCodeMapper errorCodeMapper,
                                                           ErrorMessageMapper errorMessageMapper) {
        return new BindApiExceptionHandler(properties, httpStatusMapper, errorCodeMapper, errorMessageMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiErrorResponseSerializer apiErrorResponseSerializer(ErrorHandlingProperties properties) {
        return new ApiErrorResponseSerializer(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public FallbackGraphqlExceptionHandler defaultGqlHandler(HttpStatusMapper httpStatusMapper,
                                                             ErrorCodeMapper errorCodeMapper,
                                                             ErrorMessageMapper errorMessageMapper,
                                                             GraphQlErrorMapper graphQlErrorMapper) {
        return new DefaultFallbackGraphqlExceptionHandler(httpStatusMapper,
                                                          errorCodeMapper,
                                                          errorMessageMapper,
                                                          graphQlErrorMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public GqlConstraintViolationExceptionHandler gqlConstraintViolationExceptionHandler(ErrorHandlingProperties properties,
                                                                                         HttpStatusMapper httpStatusMapper,
                                                                                         ErrorCodeMapper errorCodeMapper,
                                                                                         ErrorMessageMapper errorMessageMapper,
                                                                                         GraphQlErrorMapper graphQlErrorMapper) {
        return new GqlConstraintViolationExceptionHandler(properties,
                                                          httpStatusMapper,
                                                          errorCodeMapper,
                                                          errorMessageMapper,
                                                          graphQlErrorMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public GqlAccessDeniedExceptionHandler gqlAccessDeniedExceptionHandler(ErrorHandlingProperties properties,
                                                                           HttpStatusMapper httpStatusMapper,
                                                                           ErrorCodeMapper errorCodeMapper,
                                                                           ErrorMessageMapper errorMessageMapper,
                                                                           GraphQlErrorMapper graphQlErrorMapper) {
        return new GqlAccessDeniedExceptionHandler(properties,
                                                   httpStatusMapper,
                                                   errorCodeMapper,
                                                   errorMessageMapper,
                                                   graphQlErrorMapper);
    }
}
