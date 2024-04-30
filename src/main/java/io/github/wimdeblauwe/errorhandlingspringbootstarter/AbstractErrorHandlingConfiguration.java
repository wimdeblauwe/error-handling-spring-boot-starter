package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.handler.*;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.GraphQlErrorMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.List;

public abstract class AbstractErrorHandlingConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractErrorHandlingConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public ErrorHandlingFacade errorHandlingFacade(List<ApiExceptionHandler> handlers,
                                                   FallbackApiExceptionHandler fallbackHandler,
                                                   LoggingService loggingService,
                                                   List<ApiErrorResponseCustomizer> responseCustomizers) {
        handlers.sort(AnnotationAwareOrderComparator.INSTANCE);
        LOGGER.info("Error Handling Spring Boot Starter active with {} handlers", handlers.size());
        LOGGER.debug("Handlers: {}", handlers);

        return new ErrorHandlingFacade(handlers, fallbackHandler, loggingService, responseCustomizers);
    }

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
