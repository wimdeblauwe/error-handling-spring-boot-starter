package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class LoggingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingService.class);
    private final ErrorHandlingProperties properties;

    public LoggingService(ErrorHandlingProperties properties) {
        this.properties = properties;
    }

    public void logException(ApiErrorResponse errorResponse, Throwable exception) {
        if (properties.getFullStacktraceClasses().contains(exception.getClass())) {
            LOGGER.error(exception.getMessage(), exception);
        } else if (!properties.getFullStacktraceHttpStatuses().isEmpty()) {
            boolean alreadyLogged = logFullStacktraceIfNeeded(errorResponse.getHttpStatus(), exception);
            if (!alreadyLogged) {
                doStandardFallbackLogging(exception);
            }
        } else {
            doStandardFallbackLogging(exception);
        }
    }

    private void doStandardFallbackLogging(Throwable exception) {
        switch (properties.getExceptionLogging()) {
            case WITH_STACKTRACE:
                LOGGER.error(exception.getMessage(), exception);
                break;
            case MESSAGE_ONLY:
                LOGGER.error(exception.getMessage());
                break;
        }
    }

    private boolean logFullStacktraceIfNeeded(HttpStatus httpStatus, Throwable exception) {
        String httpStatusValue = String.valueOf(httpStatus.value());
        if (properties.getFullStacktraceHttpStatuses().contains(httpStatusValue)) {
            LOGGER.error(exception.getMessage(), exception);
            return true;
        } else if (properties.getFullStacktraceHttpStatuses().contains(httpStatusValue.replaceFirst("\\d$", "x"))) {
            LOGGER.error(exception.getMessage(), exception);
            return true;
        } else if (properties.getFullStacktraceHttpStatuses().contains(httpStatusValue.replaceFirst("\\d\\d$", "xx"))) {
            LOGGER.error(exception.getMessage(), exception);
            return true;
        }

        return false;
    }
}
