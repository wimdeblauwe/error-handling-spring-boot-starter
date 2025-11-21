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
        if (exception instanceof MissingMatrixVariableException missingMatrixVariableException) {
            response.addErrorProperty("variableName", missingMatrixVariableException.getVariableName());
            addParameterInfo(response, missingMatrixVariableException.getParameter());
        } else if (exception instanceof MissingPathVariableException missingPathVariableException) {
            response.addErrorProperty("variableName", missingPathVariableException.getVariableName());
            addParameterInfo(response, missingPathVariableException.getParameter());
        } else if (exception instanceof MissingRequestCookieException missingRequestCookieException) {
            response.addErrorProperty("cookieName", missingRequestCookieException.getCookieName());
            addParameterInfo(response, missingRequestCookieException.getParameter());
        } else if (exception instanceof MissingRequestHeaderException missingRequestHeaderException) {
            response.addErrorProperty("headerName", missingRequestHeaderException.getHeaderName());
            addParameterInfo(response, missingRequestHeaderException.getParameter());
        } else if (exception instanceof MissingServletRequestParameterException missingServletRequestParameterException) {
            String parameterName = missingServletRequestParameterException.getParameterName();
            String parameterType = missingServletRequestParameterException.getParameterType();
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
