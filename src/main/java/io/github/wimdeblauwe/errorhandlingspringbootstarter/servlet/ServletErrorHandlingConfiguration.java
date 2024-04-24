package io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;

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
    public ErrorHandlingControllerAdvice errorHandlingControllerAdvice(ErrorHandlingFacade errorHandlingFacade) {
        return new ErrorHandlingControllerAdvice(errorHandlingFacade);
    }

    @Bean
    @ConditionalOnProperty("error.handling.handle-filter-chain-exceptions")
    public FilterChainExceptionHandlerFilter filterChainExceptionHandlerFilter(ErrorHandlingFacade errorHandlingFacade, ObjectMapper objectMapper) {
        return new FilterChainExceptionHandlerFilter(errorHandlingFacade, objectMapper);
    }

    @Bean
    @ConditionalOnProperty("error.handling.handle-filter-chain-exceptions")
    public FilterRegistrationBean<FilterChainExceptionHandlerFilter> filterChainExceptionHandlerFilterFilterRegistrationBean(FilterChainExceptionHandlerFilter filterChainExceptionHandlerFilter) {
        FilterRegistrationBean<FilterChainExceptionHandlerFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(filterChainExceptionHandlerFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        return registrationBean;
    }
}
