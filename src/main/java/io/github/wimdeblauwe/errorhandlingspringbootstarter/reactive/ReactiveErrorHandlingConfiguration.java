package io.github.wimdeblauwe.errorhandlingspringbootstarter.reactive;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.AbstractErrorHandlingConfiguration;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.FallbackApiExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@EnableConfigurationProperties({ErrorHandlingProperties.class, WebProperties.class, ServerProperties.class})
@ConditionalOnProperty(value = "error.handling.enabled", matchIfMissing = true)
@PropertySource("classpath:/error-handling-defaults.properties")
public class ReactiveErrorHandlingConfiguration extends AbstractErrorHandlingConfiguration {

    @Bean
    @Order(-2)
    public ErrorWebExceptionHandler globalErrorWebExceptionHandler(ErrorAttributes errorAttributes, ServerProperties serverProperties,
                                                                   WebProperties webProperties, ObjectProvider<ViewResolver> viewResolvers,
                                                                   ServerCodecConfigurer serverCodecConfigurer, ApplicationContext applicationContext,
                                                                   ErrorHandlingProperties properties,
                                                                   List<ApiExceptionHandler> handlers,
                                                                   FallbackApiExceptionHandler fallbackApiExceptionHandler) {

        GlobalErrorWebExceptionHandler exceptionHandler = new GlobalErrorWebExceptionHandler(errorAttributes,
                                                                                             webProperties.getResources(),
                                                                                             serverProperties.getError(),
                                                                                             applicationContext,
                                                                                             properties,
                                                                                             handlers,
                                                                                             fallbackApiExceptionHandler
        );
        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        return exceptionHandler;
    }

    @Bean
    @ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
    public DefaultErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes();
    }
}
