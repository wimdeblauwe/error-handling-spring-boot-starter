package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.springframework.http.HttpStatus;

public abstract class AbstractApiExceptionHandler implements ApiExceptionHandler {
    protected final ErrorHandlingProperties properties;
    protected final HttpStatusMapper httpStatusMapper;
    protected final ErrorCodeMapper errorCodeMapper;
    protected final ErrorMessageMapper errorMessageMapper;

    public AbstractApiExceptionHandler(ErrorHandlingProperties properties,
                                       HttpStatusMapper httpStatusMapper,
                                       ErrorCodeMapper errorCodeMapper,
                                       ErrorMessageMapper errorMessageMapper) {
        this.properties = properties;
        this.httpStatusMapper = httpStatusMapper;
        this.errorCodeMapper = errorCodeMapper;
        this.errorMessageMapper = errorMessageMapper;
    }

    protected HttpStatus getHttpStatus(Throwable exception, HttpStatus defaultHttpStatus) {
        return httpStatusMapper.getHttpStatus(exception, defaultHttpStatus);
    }

    protected String getErrorCode(Throwable exception) {
        return errorCodeMapper.getErrorCode(exception);
    }

    protected String getErrorMessage(Throwable exception) {
        return errorMessageMapper.getErrorMessage(exception);
    }

    protected String replaceCodeWithConfiguredOverrideIfPresent(String code) {
        return properties.getCodes().getOrDefault(code, code);
    }

    protected boolean hasConfiguredOverrideForCode(String code) {
        return properties.getCodes().containsKey(code);
    }

    protected boolean hasConfiguredOverrideForMessage(String key) {
        return properties.getMessages().containsKey(key);
    }

    protected String getOverrideMessage(String key) {
        return properties.getMessages().get(key);
    }
}
