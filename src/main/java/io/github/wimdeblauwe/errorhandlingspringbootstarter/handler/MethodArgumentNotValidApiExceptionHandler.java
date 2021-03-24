package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiFieldError;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiGlobalError;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Class to handle {@link MethodArgumentNotValidException} exceptions. This is typically
 * used when `@Valid` is used on {@link org.springframework.web.bind.annotation.RestController}
 * method arguments.
 */
public class MethodArgumentNotValidApiExceptionHandler extends AbstractApiExceptionHandler {

    public MethodArgumentNotValidApiExceptionHandler(ErrorHandlingProperties properties) {
        super(properties);
    }

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof MethodArgumentNotValidException;
    }

    @Override
    public ApiErrorResponse handle(Throwable exception) {

        MethodArgumentNotValidException ex = (MethodArgumentNotValidException) exception;
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                                                         getErrorCode(exception),
                                                         getMessage(ex));
        BindingResult bindingResult = ex.getBindingResult();
        if (bindingResult.hasFieldErrors()) {
            bindingResult.getFieldErrors().stream()
                         .map(fieldError -> new ApiFieldError(getCode(fieldError),
                                                              fieldError.getField(),
                                                              getMessage(fieldError),
                                                              fieldError.getRejectedValue()))
                         .forEach(response::addFieldError);
        }

        if (bindingResult.hasGlobalErrors()) {
            bindingResult.getGlobalErrors().stream()
                         .map(globalError -> new ApiGlobalError(replaceCodeWithConfiguredOverrideIfPresent(globalError.getCode()),
                                                                getMessage(globalError)))
                         .forEach(response::addGlobalError);
        }

        return response;
    }

    private String getCode(FieldError fieldError) {
        String fieldSpecificCode = fieldError.getField() + "." + fieldError.getCode();
        if (hasConfiguredOverrideForCode(fieldSpecificCode)) {
            return replaceCodeWithConfiguredOverrideIfPresent(fieldSpecificCode);
        }
        return replaceCodeWithConfiguredOverrideIfPresent(fieldError.getCode());
    }

    private String getMessage(FieldError fieldError) {
        String fieldSpecificKey = fieldError.getField() + "." + fieldError.getCode();
        if (hasConfiguredOverrideForMessage(fieldSpecificKey)) {
            return getOverrideMessage(fieldSpecificKey);
        }
        if (hasConfiguredOverrideForMessage(fieldError.getCode())) {
            return getOverrideMessage(fieldError.getCode());
        }
        return fieldError.getDefaultMessage();
    }

    private String getMessage(ObjectError objectError) {
        if (hasConfiguredOverrideForMessage(objectError.getCode())) {
            return getOverrideMessage(objectError.getCode());
        }
        return objectError.getDefaultMessage();
    }

    private String getMessage(MethodArgumentNotValidException exception) {
        return "Validation failed for object='" + exception.getBindingResult().getObjectName() + "'. Error count: " + exception.getBindingResult().getErrorCount();
    }
}
