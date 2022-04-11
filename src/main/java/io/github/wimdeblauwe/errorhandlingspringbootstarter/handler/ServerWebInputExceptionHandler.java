package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

public class ServerWebInputExceptionHandler extends AbstractApiExceptionHandler {
    public ServerWebInputExceptionHandler(HttpStatusMapper httpStatusMapper,
                                          ErrorCodeMapper errorCodeMapper,
                                          ErrorMessageMapper errorMessageMapper) {
        super(httpStatusMapper, errorCodeMapper, errorMessageMapper);
    }

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof ServerWebInputException
                // WebExchangeBindException should be handled by BindApiExceptionHandler
                && !(exception instanceof WebExchangeBindException);
    }

    @Override
    public ApiErrorResponse handle(Throwable exception) {
        ServerWebInputException ex = (ServerWebInputException) exception;
        HttpStatus status = ex.getStatus();
        ApiErrorResponse response = new ApiErrorResponse(status,
                                                         getErrorCode(exception),
                                                         getErrorMessage(exception));
        MethodParameter methodParameter = ex.getMethodParameter();
        if (methodParameter != null) {
            response.addErrorProperty("parameterName", methodParameter.getParameterName());
            response.addErrorProperty("parameterType", methodParameter.getParameterType().getSimpleName());
        }
        return response;
    }
}
