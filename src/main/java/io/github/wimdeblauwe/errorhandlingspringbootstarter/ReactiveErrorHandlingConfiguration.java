package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.handler.*;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.*;
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
public class ReactiveErrorHandlingConfiguration {

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
    public HttpStatusMapper httpStatusMapper(ErrorHandlingProperties properties) {
        return new HttpStatusMapper(properties);
    }

    @Bean
    public ErrorCodeMapper errorCodeMapper(ErrorHandlingProperties properties) {
        return new ErrorCodeMapper(properties);
    }

    @Bean
    public ErrorMessageMapper errorMessageMapper(ErrorHandlingProperties properties) {
        return new ErrorMessageMapper(properties);
    }

    @Bean
    public FallbackApiExceptionHandler defaultHandler(HttpStatusMapper httpStatusMapper,
                                                      ErrorCodeMapper errorCodeMapper,
                                                      ErrorMessageMapper errorMessageMapper) {
        return new DefaultFallbackApiExceptionHandler(httpStatusMapper,
                                                      errorCodeMapper,
                                                      errorMessageMapper);
    }

    @Bean
    public TypeMismatchApiExceptionHandler typeMismatchApiExceptionHandler(ErrorHandlingProperties properties,
                                                                           HttpStatusMapper httpStatusMapper,
                                                                           ErrorCodeMapper errorCodeMapper,
                                                                           ErrorMessageMapper errorMessageMapper) {
        return new TypeMismatchApiExceptionHandler(properties, httpStatusMapper, errorCodeMapper, errorMessageMapper);
    }

    @Bean
    public ConstraintViolationApiExceptionHandler constraintViolationApiExceptionHandler(ErrorHandlingProperties properties,
                                                                                         HttpStatusMapper httpStatusMapper,
                                                                                         ErrorCodeMapper errorCodeMapper,
                                                                                         ErrorMessageMapper errorMessageMapper) {
        return new ConstraintViolationApiExceptionHandler(properties, httpStatusMapper, errorCodeMapper, errorMessageMapper);
    }

    @Bean
    public HttpMessageNotReadableApiExceptionHandler httpMessageNotReadableApiExceptionHandler(ErrorHandlingProperties properties,
                                                                                               HttpStatusMapper httpStatusMapper,
                                                                                               ErrorCodeMapper errorCodeMapper,
                                                                                               ErrorMessageMapper errorMessageMapper) {
        return new HttpMessageNotReadableApiExceptionHandler(properties, httpStatusMapper, errorCodeMapper, errorMessageMapper);
    }

    @Bean
    public BindApiExceptionHandler bindApiExceptionHandler(ErrorHandlingProperties properties,
                                                           HttpStatusMapper httpStatusMapper,
                                                           ErrorCodeMapper errorCodeMapper,
                                                           ErrorMessageMapper errorMessageMapper) {
        return new BindApiExceptionHandler(properties, httpStatusMapper, errorCodeMapper, errorMessageMapper);
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.security.access.AccessDeniedException")
    public SpringSecurityApiExceptionHandler springSecurityApiExceptionHandler(ErrorHandlingProperties properties,
                                                                               HttpStatusMapper httpStatusMapper,
                                                                               ErrorCodeMapper errorCodeMapper,
                                                                               ErrorMessageMapper errorMessageMapper) {
        return new SpringSecurityApiExceptionHandler(properties, httpStatusMapper, errorCodeMapper, errorMessageMapper);
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.orm.ObjectOptimisticLockingFailureException")
    public ObjectOptimisticLockingFailureApiExceptionHandler objectOptimisticLockingFailureApiExceptionHandler(ErrorHandlingProperties properties,
                                                                                                               HttpStatusMapper httpStatusMapper,
                                                                                                               ErrorCodeMapper errorCodeMapper,
                                                                                                               ErrorMessageMapper errorMessageMapper) {
        return new ObjectOptimisticLockingFailureApiExceptionHandler(properties, httpStatusMapper, errorCodeMapper, errorMessageMapper);
    }

    @Bean
    public ApiErrorResponseSerializer apiErrorResponseSerializer(ErrorHandlingProperties properties) {
        return new ApiErrorResponseSerializer(properties);
    }

    @Bean
    @ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
    public DefaultErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes();
    }
}
