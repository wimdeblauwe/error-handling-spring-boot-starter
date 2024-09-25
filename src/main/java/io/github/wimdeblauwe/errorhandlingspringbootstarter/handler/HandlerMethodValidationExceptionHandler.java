package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiFieldError;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiGlobalError;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;
import java.util.Optional;

public class HandlerMethodValidationExceptionHandler extends AbstractApiExceptionHandler {

    public HandlerMethodValidationExceptionHandler(HttpStatusMapper httpStatusMapper,
                                                   ErrorCodeMapper errorCodeMapper,
                                                   ErrorMessageMapper errorMessageMapper) {

        super(httpStatusMapper, errorCodeMapper, errorMessageMapper);
    }

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof HandlerMethodValidationException;
    }

    @Override
    public ApiErrorResponse handle(Throwable ex) {
        var response = new ApiErrorResponse(HttpStatus.BAD_REQUEST, getErrorCode(ex), getErrorMessage(ex));
        var validationException = (HandlerMethodValidationException) ex;
        List<? extends MessageSourceResolvable> errors = validationException.getAllErrors();

        errors.forEach(error -> {
            if (error instanceof FieldError fieldError) {
                var apiFieldError = new ApiFieldError(
                        errorCodeMapper.getErrorCode(fieldError.getCode()),
                        fieldError.getField(),
                        errorMessageMapper.getErrorMessage(fieldError.getCode(), fieldError.getDefaultMessage()),
                        fieldError.getRejectedValue(),
                        null);
                response.addFieldError(apiFieldError);
            } else {
                var lastCode = Optional.ofNullable(error.getCodes())
                                       .filter(codes -> codes.length > 0)
                                       .map(codes -> codes[codes.length - 1])
                                       .orElse(null);
                var apiGlobalErrorMessage = new ApiGlobalError(
                        errorCodeMapper.getErrorCode(lastCode),
                        errorMessageMapper.getErrorMessage(lastCode, error.getDefaultMessage()));
                response.addGlobalError(apiGlobalErrorMessage);
            }
        });

        return response;
    }
}
