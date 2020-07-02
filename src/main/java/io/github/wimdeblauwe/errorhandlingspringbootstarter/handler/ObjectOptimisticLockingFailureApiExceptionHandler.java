package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

public class ObjectOptimisticLockingFailureApiExceptionHandler extends AbstractApiExceptionHandler {

    public ObjectOptimisticLockingFailureApiExceptionHandler(ErrorHandlingProperties properties) {
        super(properties);
    }

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof ObjectOptimisticLockingFailureException;
    }

    @Override
    public ApiErrorResponse handle(Throwable exception) {
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.CONFLICT,
                                                         getErrorCode(exception),
                                                         exception.getMessage());
        ObjectOptimisticLockingFailureException ex = (ObjectOptimisticLockingFailureException) exception;
        response.addErrorProperty("identifier", ex.getIdentifier());
        response.addErrorProperty("persistentClassName", ex.getPersistentClassName());
        return response;
    }
}
