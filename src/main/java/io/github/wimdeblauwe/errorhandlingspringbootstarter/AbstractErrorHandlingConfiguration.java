package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.handler.BindApiExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.handler.HandlerMethodValidationExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.handler.HttpMessageNotReadableApiExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.handler.TypeMismatchApiExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.client.RestClientResponseException;

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
    public LoggingService loggingService(ErrorHandlingProperties properties) {
        return new LoggingService(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpStatusMapper httpStatusMapper(ErrorHandlingProperties properties,
                                             List<HttpResponseStatusFromExceptionMapper> httpResponseStatusFromExceptionMapperList) {
        return new HttpStatusMapper(properties, httpResponseStatusFromExceptionMapperList);
    }

    @Bean
    public ResponseStatusExceptionHttpResponseStatusFromExceptionMapper responseStatusExceptionHttpResponseStatusFromExceptionMapper() {
        return new ResponseStatusExceptionHttpResponseStatusFromExceptionMapper();
    }

    @Bean
    @ConditionalOnClass(RestClientResponseException.class)
    public RestClientResponseExceptionHttpResponseStatusFromExceptionMapper restClientResponseExceptionHttpResponseStatusFromExceptionMapper() {
        return new RestClientResponseExceptionHttpResponseStatusFromExceptionMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorCodeMapper errorCodeMapper(ErrorHandlingProperties properties) {
        return new ErrorCodeMapper(properties);
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
    public HandlerMethodValidationExceptionHandler handlerMethodValidationExceptionHandler(HttpStatusMapper httpStatusMapper,
                                                                                           ErrorCodeMapper errorCodeMapper,
                                                                                           ErrorMessageMapper errorMessageMapper) {
        return new HandlerMethodValidationExceptionHandler(httpStatusMapper, errorCodeMapper, errorMessageMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiErrorResponseSerializer apiErrorResponseSerializer(ErrorHandlingProperties properties) {
        return new ApiErrorResponseSerializer(properties);
    }
}
