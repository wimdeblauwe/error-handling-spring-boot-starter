package io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.AbstractErrorHandlingConfiguration;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.FallbackApiExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(ErrorHandlingProperties.class)
@ConditionalOnProperty(value = "error.handling.enabled", matchIfMissing = true)
@PropertySource("classpath:/error-handling-defaults.properties")
public class ServletErrorHandlingConfiguration extends AbstractErrorHandlingConfiguration {

    @Bean
    public ErrorHandlingControllerAdvice errorHandlingControllerAdvice(ErrorHandlingProperties properties,
                                                                       List<ApiExceptionHandler> handlers,
                                                                       FallbackApiExceptionHandler fallbackApiExceptionHandler) {
        return new ErrorHandlingControllerAdvice(properties,
                                                 handlers,
                                                 fallbackApiExceptionHandler);
    }
}
