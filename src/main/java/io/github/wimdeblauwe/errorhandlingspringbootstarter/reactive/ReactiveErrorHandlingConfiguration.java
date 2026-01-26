package io.github.wimdeblauwe.errorhandlingspringbootstarter.reactive;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.*;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.handler.ServerErrorExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.handler.ServerWebInputExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.boot.webflux.error.DefaultErrorAttributes;
import org.springframework.boot.webflux.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.stream.Collectors;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@EnableConfigurationProperties({ErrorHandlingProperties.class, WebProperties.class, ServerProperties.class})
@ConditionalOnProperty(value = "error.handling.enabled", matchIfMissing = true)
@PropertySource("classpath:/error-handling-defaults.properties")
@Import({ValidationErrorHandlingConfiguration.class,
        SpringSecurityErrorHandlingConfiguration.class,
        SpringOrmErrorHandlingConfiguration.class})
public class ReactiveErrorHandlingConfiguration extends AbstractErrorHandlingConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServerWebInputExceptionHandler serverWebInputExceptionHandler(HttpStatusMapper httpStatusMapper,
                                                                         ErrorCodeMapper errorCodeMapper,
                                                                         ErrorMessageMapper errorMessageMapper) {
        return new ServerWebInputExceptionHandler(httpStatusMapper,
                                                  errorCodeMapper,
                                                  errorMessageMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServerErrorExceptionHandler serverErrorExceptionHandler(HttpStatusMapper httpStatusMapper,
                                                                   ErrorCodeMapper errorCodeMapper,
                                                                   ErrorMessageMapper errorMessageMapper) {
        return new ServerErrorExceptionHandler(httpStatusMapper,
                                               errorCodeMapper,
                                               errorMessageMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    @Order(-2)
    public GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler(ErrorAttributes errorAttributes,
                                                                         WebProperties webProperties,
                                                                         ObjectProvider<ViewResolver> viewResolvers,
                                                                         ServerCodecConfigurer serverCodecConfigurer,
                                                                         ApplicationContext applicationContext,
                                                                         ErrorHandlingFacade errorHandlingFacade,
                                                                         ReactiveResponseFactory responseEntityFactory) {

        GlobalErrorWebExceptionHandler exceptionHandler = new GlobalErrorWebExceptionHandler(errorAttributes,
                                                                                             webProperties.getResources(),
                                                                                             webProperties.getError(),
                                                                                             applicationContext,
                                                                                             errorHandlingFacade,
                                                                                             responseEntityFactory
        );
        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        return exceptionHandler;
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactiveResponseFactory responseEntityFactory(ErrorHandlingProperties errorHandlingProperties, ProblemDetailFactory problemDetailFactory) {
        if (errorHandlingProperties.isUseProblemDetailFormat()) {
            return new ReactiveProblemDetailResponseFactory(problemDetailFactory);
        } else {
            return new ReactiveDefaultResponseFactory();
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public ProblemDetailFactory problemDetailFactory(ErrorHandlingProperties errorHandlingProperties) {
        return new ProblemDetailFactory(errorHandlingProperties);
    }

    @Bean
    @ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
    public DefaultErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes();
    }
}
