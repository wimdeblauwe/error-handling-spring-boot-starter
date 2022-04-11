package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.handler.ObjectOptimisticLockingFailureApiExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@Configuration
@ConditionalOnClass(ObjectOptimisticLockingFailureException.class)
public class SpringOrmErrorHandlingConfiguration {
    @Bean
    public ObjectOptimisticLockingFailureApiExceptionHandler objectOptimisticLockingFailureApiExceptionHandler(ErrorHandlingProperties properties,
                                                                                                               HttpStatusMapper httpStatusMapper,
                                                                                                               ErrorCodeMapper errorCodeMapper,
                                                                                                               ErrorMessageMapper errorMessageMapper) {
        return new ObjectOptimisticLockingFailureApiExceptionHandler(properties, httpStatusMapper, errorCodeMapper, errorMessageMapper);
    }

}
