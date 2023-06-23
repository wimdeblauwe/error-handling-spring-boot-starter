package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.handler.SpringSecurityApiExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;

@Configuration
@ConditionalOnClass(AccessDeniedException.class)
public class SpringSecurityErrorHandlingConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public SpringSecurityApiExceptionHandler springSecurityApiExceptionHandler(ErrorHandlingProperties properties,
                                                                               HttpStatusMapper httpStatusMapper,
                                                                               ErrorCodeMapper errorCodeMapper,
                                                                               ErrorMessageMapper errorMessageMapper) {
        return new SpringSecurityApiExceptionHandler(properties, httpStatusMapper, errorCodeMapper, errorMessageMapper);
    }
}
