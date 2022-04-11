package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.*;

public class MissingRequestValueExceptionHandler extends AbstractApiExceptionHandler {
    public MissingRequestValueExceptionHandler(HttpStatusMapper httpStatusMapper,
                                               ErrorCodeMapper errorCodeMapper,
                                               ErrorMessageMapper errorMessageMapper) {
        super(httpStatusMapper, errorCodeMapper, errorMessageMapper);
    }

    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof MissingRequestValueException;
    }

    @Override
    public ApiErrorResponse handle(Throwable exception) {
        ApiErrorResponse response = new ApiErrorResponse(getHttpStatus(exception),
                                                         getErrorCode(exception),
                                                         getErrorMessage(exception));
        if (exception instanceof MissingMatrixVariableException) {
            response.addErrorProperty("variableName", ((MissingMatrixVariableException) exception).getVariableName());
            addParameterInfo(response, ((MissingMatrixVariableException) exception).getParameter());
        } else if (exception instanceof MissingPathVariableException) {
            response.addErrorProperty("variableName", ((MissingPathVariableException) exception).getVariableName());
            addParameterInfo(response, ((MissingPathVariableException) exception).getParameter());
        } else if (exception instanceof MissingRequestCookieException) {
            response.addErrorProperty("cookieName", ((MissingRequestCookieException) exception).getCookieName());
            addParameterInfo(response, ((MissingRequestCookieException) exception).getParameter());
        } else if (exception instanceof MissingRequestHeaderException) {
            response.addErrorProperty("headerName", ((MissingRequestHeaderException) exception).getHeaderName());
            addParameterInfo(response, ((MissingRequestHeaderException) exception).getParameter());
        } else if (exception instanceof MissingServletRequestParameterException) {
            String parameterName = ((MissingServletRequestParameterException) exception).getParameterName();
            String parameterType = ((MissingServletRequestParameterException) exception).getParameterType();
            response.addErrorProperty("parameterName", parameterName);
            response.addErrorProperty("parameterType", parameterType);
        }
        return response;
    }

    private void addParameterInfo(ApiErrorResponse response, MethodParameter parameter) {
        response.addErrorProperty("parameterName", parameter.getParameterName());
        response.addErrorProperty("parameterType", parameter.getParameterType().getSimpleName());
    }

    private HttpStatus getHttpStatus(Throwable exception) {
        if (exception instanceof MissingPathVariableException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.BAD_REQUEST;
    }
}
