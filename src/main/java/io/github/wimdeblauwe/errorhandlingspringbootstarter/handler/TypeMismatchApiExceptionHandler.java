package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

public class TypeMismatchApiExceptionHandler extends AbstractApiExceptionHandler {
    public TypeMismatchApiExceptionHandler(ErrorHandlingProperties properties) {
        super(properties);
    }

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof TypeMismatchException;
    }

    @Override
    public ApiErrorResponse handle(Throwable exception) {
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                                                         getErrorCode(exception),
                                                         exception.getMessage());
        TypeMismatchException ex = (TypeMismatchException) exception;
        response.addErrorProperty("property", getPropertyName(ex));
        response.addErrorProperty("rejectedValue", ex.getValue());
        response.addErrorProperty("expectedType", ex.getRequiredType() != null ? ex.getRequiredType().getName() : null);
        return response;
    }

    private String getPropertyName(TypeMismatchException exception) {
        if (exception instanceof MethodArgumentTypeMismatchException) {
            return ((MethodArgumentTypeMismatchException) exception).getName();
        } else {
            return exception.getPropertyName();
        }
    }
}
