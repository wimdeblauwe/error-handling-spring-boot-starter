package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;

/**
 * {@link io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiExceptionHandler} for
 * {@link HttpMessageNotReadableException}. This typically happens when Spring can't properly
 * decode the incoming request to JSON.
 */
public class HttpMessageNotReadableApiExceptionHandler extends AbstractApiExceptionHandler {
    public HttpMessageNotReadableApiExceptionHandler(ErrorHandlingProperties properties,
                                                     HttpStatusMapper httpStatusMapper,
                                                     ErrorCodeMapper errorCodeMapper,
                                                     ErrorMessageMapper errorMessageMapper) {
        super(properties, httpStatusMapper, errorCodeMapper, errorMessageMapper);
    }

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof HttpMessageNotReadableException;
    }

    @Override
    public ApiErrorResponse handle(Throwable exception) {
        return new ApiErrorResponse(getHttpStatus(exception, HttpStatus.BAD_REQUEST),
                                    getErrorCode(exception),
                                    getErrorMessage(exception));
    }

}
