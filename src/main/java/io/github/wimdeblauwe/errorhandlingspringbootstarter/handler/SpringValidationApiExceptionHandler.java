package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.*;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

public class SpringValidationApiExceptionHandler implements ApiExceptionHandler {

    private final ErrorHandlingProperties properties;

    public SpringValidationApiExceptionHandler(ErrorHandlingProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof MethodArgumentNotValidException;
    }

    @Override
    public ApiErrorResponse handle(Throwable exception) {
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                                                         replaceCodeWithConfiguredOverrideIfPresent(exception.getClass().getName()),
                                                         getMessage((MethodArgumentNotValidException)exception));

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

        return response;
    }

    private String getMessage(MethodArgumentNotValidException exception) {
        return "Validation failed for object='" + exception.getBindingResult().getObjectName() + "'. Error count: " + exception.getBindingResult().getErrorCount();
    }

    private String replaceCodeWithConfiguredOverrideIfPresent(String code) {
        return properties.getCodes().getOrDefault(code, code);
    }
}
