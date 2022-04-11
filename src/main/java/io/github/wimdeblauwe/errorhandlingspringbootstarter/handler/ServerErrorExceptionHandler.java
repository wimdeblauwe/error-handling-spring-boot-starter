package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.springframework.core.MethodParameter;
import org.springframework.web.server.ServerErrorException;

import java.lang.reflect.Method;

public class ServerErrorExceptionHandler extends AbstractApiExceptionHandler {
    public ServerErrorExceptionHandler(HttpStatusMapper httpStatusMapper,
                                       ErrorCodeMapper errorCodeMapper,
                                       ErrorMessageMapper errorMessageMapper) {
        super(httpStatusMapper, errorCodeMapper, errorMessageMapper);
    }

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof ServerErrorException;
    }

    @Override
    public ApiErrorResponse handle(Throwable exception) {
        ServerErrorException ex = (ServerErrorException) exception;
        ApiErrorResponse response = new ApiErrorResponse(ex.getStatus(),
                                                         getErrorCode(ex),
                                                         getErrorMessage(ex));
        MethodParameter methodParameter = ex.getMethodParameter();
        if (methodParameter != null) {
            response.addErrorProperty("parameterName", methodParameter.getParameterName());
            response.addErrorProperty("parameterType", methodParameter.getParameterType().getSimpleName());
        }

        Method handlerMethod = ex.getHandlerMethod();
        if (handlerMethod != null) {
            response.addErrorProperty("methodName", handlerMethod.getName());
            response.addErrorProperty("methodClassName", handlerMethod.getDeclaringClass().getSimpleName());
        }

        return response;
    }
}
