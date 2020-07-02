package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

public class ObjectOptimisticLockingFailureApiExceptionHandler implements ApiExceptionHandler {
    private final ErrorHandlingProperties properties;

    public ObjectOptimisticLockingFailureApiExceptionHandler(ErrorHandlingProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof ObjectOptimisticLockingFailureException;
    }

    @Override
    public ApiErrorResponse handle(Throwable exception) {
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.CONFLICT,
                                                         replaceCodeWithConfiguredOverrideIfPresent(exception.getClass().getName()),
                                                         exception.getMessage());
        ObjectOptimisticLockingFailureException ex = (ObjectOptimisticLockingFailureException) exception;
        response.addErrorProperty("identifier", ex.getIdentifier());
        response.addErrorProperty("persistentClassName", ex.getPersistentClassName());
        return response;
    }

    private String replaceCodeWithConfiguredOverrideIfPresent(String code) {
        return properties.getCodes().getOrDefault(code, code);
    }

}
