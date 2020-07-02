package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.handler.SpringValidationApiExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(ErrorHandlingProperties.class)
@ConditionalOnProperty(value = "error.handling.enabled", matchIfMissing = true)
@PropertySource("classpath:/error-handling-defaults.properties")
public class ErrorHandlingConfiguration {
    @Bean
    public ErrorHandlingControllerAdvice errorHandlingControllerAdvice(ErrorHandlingProperties properties,
                                                                       List<ApiExceptionHandler> handlers,
                                                                       FallbackApiExceptionHandler fallbackApiExceptionHandler) {
        return new ErrorHandlingControllerAdvice(properties,
                                                 handlers,
                                                 fallbackApiExceptionHandler);
    }

    @Bean
    public FallbackApiExceptionHandler defaultHandler(ErrorHandlingProperties properties) {
        return new DefaultFallbackApiExceptionHandler(properties);
    }

    @Bean
    public SpringValidationApiExceptionHandler springValidationApiExceptionHandler(ErrorHandlingProperties properties) {
        return new SpringValidationApiExceptionHandler(properties);
    }
}
