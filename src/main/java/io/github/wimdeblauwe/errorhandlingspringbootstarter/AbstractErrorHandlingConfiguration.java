package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.handler.BindApiExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.handler.HttpMessageNotReadableApiExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.handler.TypeMismatchApiExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

public abstract class AbstractErrorHandlingConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public LoggingService loggingService(ErrorHandlingProperties properties) {
        return new LoggingService(properties);
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
}
