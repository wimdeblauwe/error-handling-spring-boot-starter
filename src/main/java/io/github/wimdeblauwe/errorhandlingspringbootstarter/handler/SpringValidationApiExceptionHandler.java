package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiFieldError;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiGlobalError;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

public class SpringValidationApiExceptionHandler extends AbstractApiExceptionHandler {

    public SpringValidationApiExceptionHandler(ErrorHandlingProperties properties) {
        super(properties);
    }

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof MethodArgumentNotValidException
                || exception instanceof HttpMessageNotReadableException;
    }

    @Override
    public ApiErrorResponse handle(Throwable exception) {

        ApiErrorResponse response;
        if (exception instanceof MethodArgumentNotValidException) {
            response = new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                                            getErrorCode(exception),
                                            getMessage((MethodArgumentNotValidException) exception));
            BindingResult bindingResult = ((MethodArgumentNotValidException) exception).getBindingResult();
            if (bindingResult.hasFieldErrors()) {
                bindingResult.getFieldErrors().stream()
                             .map(fieldError -> new ApiFieldError(replaceCodeWithConfiguredOverrideIfPresent(fieldError.getCode()),
                                                                  fieldError.getField(),
                                                                  fieldError.getDefaultMessage(),
                                                                  fieldError.getRejectedValue()))
                             .forEach(response::addFieldError);
            }

            if (bindingResult.hasGlobalErrors()) {
                bindingResult.getGlobalErrors().stream()
                             .map(globalError -> new ApiGlobalError(replaceCodeWithConfiguredOverrideIfPresent(globalError.getCode()),
                                                                    globalError.getDefaultMessage()))
                             .forEach(response::addGlobalError);
            }
        } else if (exception instanceof HttpMessageNotReadableException) {
            response = new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                                            replaceCodeWithConfiguredOverrideIfPresent(exception.getClass().getName()),
                                            exception.getMessage());

        } else {
            throw new IllegalStateException("canHandle() and handle() methods are not in sync!");
        }

        return response;
    }

    private String getMessage(MethodArgumentNotValidException exception) {
        return "Validation failed for object='" + exception.getBindingResult().getObjectName() + "'. Error count: " + exception.getBindingResult().getErrorCount();
    }
}
