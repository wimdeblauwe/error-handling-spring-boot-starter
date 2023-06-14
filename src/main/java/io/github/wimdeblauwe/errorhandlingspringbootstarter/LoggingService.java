package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatusCode;

public class LoggingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingService.class);
    private final ErrorHandlingProperties properties;

    public LoggingService(ErrorHandlingProperties properties) {
        this.properties = properties;
    }

    public void logException(ApiErrorResponse errorResponse, Throwable exception) {
        HttpStatusCode httpStatus = errorResponse.getHttpStatus();
        if (properties.getFullStacktraceClasses().contains(exception.getClass())) {
            logAccordingToRequestedLogLevel(httpStatus, exception, true);
        } else if (!properties.getFullStacktraceHttpStatuses().isEmpty()) {
            boolean alreadyLogged = logFullStacktraceIfNeeded(httpStatus, exception);
            if (!alreadyLogged) {
                doStandardFallbackLogging(httpStatus, exception);
            }
        } else {
            doStandardFallbackLogging(httpStatus, exception);
        }
    }

    private void logAccordingToRequestedLogLevel(HttpStatusCode httpStatus, Throwable exception, boolean includeStacktrace) {
        String httpStatusValue = String.valueOf(httpStatus.value());
        if (properties.getLogLevels().get(httpStatusValue) != null) {
            doLogOnLogLevel(properties.getLogLevels().get(httpStatusValue), exception, includeStacktrace);
        } else if (properties.getLogLevels().get(getStatusWithLastNumberAsWildcard(httpStatusValue)) != null) {
            doLogOnLogLevel(properties.getLogLevels().get(getStatusWithLastNumberAsWildcard(httpStatusValue)), exception, includeStacktrace);
        } else if (properties.getLogLevels().get(getStatusWithLastTwoNumbersAsWildcard(httpStatusValue)) != null) {
            doLogOnLogLevel(properties.getLogLevels().get(getStatusWithLastTwoNumbersAsWildcard(httpStatusValue)), exception, includeStacktrace);
        } else {
            doLogOnLogLevel(LogLevel.ERROR, exception, includeStacktrace);
        }
    }

    private void doLogOnLogLevel(LogLevel logLevel, Throwable exception, boolean includeStacktrace) {
        if (includeStacktrace) {
            switch (logLevel) {
                case TRACE -> LOGGER.trace(exception.getMessage(), exception);
                case DEBUG -> LOGGER.debug(exception.getMessage(), exception);
                case INFO -> LOGGER.info(exception.getMessage(), exception);
                case WARN -> LOGGER.warn(exception.getMessage(), exception);
                case ERROR, FATAL -> LOGGER.error(exception.getMessage(), exception);
                case OFF -> {
                    // no-op
                }
            }
        } else {
            switch (logLevel) {
                case TRACE -> LOGGER.trace(exception.getMessage());
                case DEBUG -> LOGGER.debug(exception.getMessage());
                case INFO -> LOGGER.info(exception.getMessage());
                case WARN -> LOGGER.warn(exception.getMessage());
                case ERROR, FATAL -> LOGGER.error(exception.getMessage());
                case OFF -> {
                    // no-op
                }
            }
        }
    }

    private void doStandardFallbackLogging(HttpStatusCode httpStatus, Throwable exception) {
        switch (properties.getExceptionLogging()) {
            case WITH_STACKTRACE -> logAccordingToRequestedLogLevel(httpStatus, exception, true);
            case MESSAGE_ONLY -> logAccordingToRequestedLogLevel(httpStatus, exception, false);
        }
    }

    private boolean logFullStacktraceIfNeeded(HttpStatusCode httpStatus, Throwable exception) {
        String httpStatusValue = String.valueOf(httpStatus.value());
        if (properties.getFullStacktraceHttpStatuses().contains(httpStatusValue)) {
            logAccordingToRequestedLogLevel(httpStatus, exception, true);
            return true;
        } else if (properties.getFullStacktraceHttpStatuses().contains(getStatusWithLastNumberAsWildcard(httpStatusValue))) {
            logAccordingToRequestedLogLevel(httpStatus, exception, true);
            return true;
        } else if (properties.getFullStacktraceHttpStatuses().contains(getStatusWithLastTwoNumbersAsWildcard(httpStatusValue))) {
            logAccordingToRequestedLogLevel(httpStatus, exception, true);
            return true;
        }

        return false;
    }

    private static String getStatusWithLastTwoNumbersAsWildcard(String httpStatusValue) {
        return httpStatusValue.replaceFirst("\\d\\d$", "xx");
    }

    private static String getStatusWithLastNumberAsWildcard(String httpStatusValue) {
        return httpStatusValue.replaceFirst("\\d$", "x");
    }
}
