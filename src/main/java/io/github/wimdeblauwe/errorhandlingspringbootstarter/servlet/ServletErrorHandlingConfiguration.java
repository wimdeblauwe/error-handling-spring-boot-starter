package io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.*;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.handler.MissingRequestValueExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(ErrorHandlingProperties.class)
@ConditionalOnProperty(value = "error.handling.enabled", matchIfMissing = true)
@PropertySource("classpath:/error-handling-defaults.properties")
@Import({ValidationErrorHandlingConfiguration.class,
        SpringSecurityErrorHandlingConfiguration.class,
        SpringOrmErrorHandlingConfiguration.class})
public class ServletErrorHandlingConfiguration extends AbstractErrorHandlingConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MissingRequestValueExceptionHandler missingRequestValueExceptionHandler(HttpStatusMapper httpStatusMapper,
                                                                                   ErrorCodeMapper errorCodeMapper,
                                                                                   ErrorMessageMapper errorMessageMapper) {
        return new MissingRequestValueExceptionHandler(httpStatusMapper,
                                                       errorCodeMapper,
                                                       errorMessageMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorHandlingControllerAdvice errorHandlingControllerAdvice(List<ApiExceptionHandler> handlers,
                                                                       FallbackApiExceptionHandler fallbackApiExceptionHandler,
                                                                       LoggingService loggingService,
                                                                       List<ApiErrorResponseCustomizer> responseCustomizers) {
        return new ErrorHandlingControllerAdvice(handlers,
                                                 fallbackApiExceptionHandler,
                                                 loggingService,
                                                 responseCustomizers);
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorHandlingDataFetcherExceptionResolverAdapter exceptionResolverAdapter(List<GraphqlExceptionHandler> handlers,
                                                                                     FallbackGraphqlExceptionHandler fallbackGraphqlExceptionHandler,
                                                                                     LoggingService loggingService) {
        return new ErrorHandlingDataFetcherExceptionResolverAdapter(handlers,
                                                                    fallbackGraphqlExceptionHandler,
                                                                    loggingService);
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorHandlingDataFetchingExceptionHandler dataFetchingExceptionHandler(List<GraphqlExceptionHandler> handlers,
                                                                                  FallbackGraphqlExceptionHandler fallbackGraphqlExceptionHandler,
                                                                                  LoggingService loggingService) {
        return new ErrorHandlingDataFetchingExceptionHandler(handlers,
                                                             fallbackGraphqlExceptionHandler,
                                                             loggingService);
    }
}
