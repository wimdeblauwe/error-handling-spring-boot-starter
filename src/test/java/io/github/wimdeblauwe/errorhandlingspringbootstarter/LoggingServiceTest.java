package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class LoggingServiceTest {

    private ErrorHandlingProperties properties;
    private LoggingService loggingService;

    @SuppressWarnings("NullAway")
    @BeforeEach
    void setUp() {
        properties = new ErrorHandlingProperties();
    }

    @Nested
    class WhenFilterReturnsFalse {

        @SuppressWarnings("NullAway")
        @BeforeEach
        void setUp() {
            loggingService = new LoggingService(properties, (errorResponse, exception) -> false);
        }

        @Test
        void shouldNotLog(CapturedOutput output) {
            ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, "TEST", "Test message");
            RuntimeException exception = new RuntimeException("Test exception");

            loggingService.logException(errorResponse, exception);

            assertThat(output.getAll()).isEmpty();
        }
    }

    @Nested
    class WhenFilterUsesCustomLogic {

        @Test
        void shouldNotLogWhenFilterExcludesBasedOnErrorCode(CapturedOutput output) {
            LoggingServiceFilter filter = (errorResponse, exception) ->
                    !"IGNORED_ERROR".equals(errorResponse.getCode());
            loggingService = new LoggingService(properties, filter);

            ApiErrorResponse ignoredResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, "IGNORED_ERROR", "This should be ignored");
            ApiErrorResponse otherResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, "OTHER", "Other exception");
            RuntimeException exception = new RuntimeException("Exception for ignored error code");
            RuntimeException otherException = new RuntimeException("Other exception");

            loggingService.logException(ignoredResponse, exception);
            loggingService.logException(otherResponse, otherException);

            assertThat(output.getAll())
                    .doesNotContain("Exception for ignored error code")
                    .contains("Other exception");
        }

        @Test
        void shouldNotLogWhenFilterExcludesBasedOnExceptionType(CapturedOutput output) {
            LoggingServiceFilter filter = (errorResponse, exception) ->
                    !(exception instanceof IllegalStateException);
            loggingService = new LoggingService(properties, filter);

            ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR", "Test message");
            IllegalStateException exception = new IllegalStateException("IllegalStateException should be filtered");
            RuntimeException otherException = new RuntimeException("This should be logged");

            loggingService.logException(errorResponse, exception);
            loggingService.logException(errorResponse, otherException);

            assertThat(output.getAll())
                    .doesNotContain("IllegalStateException should be filtered")
                    .contains("This should be logged");
        }
    }

    @Nested
    class WhenFilterReturnsTrue {

        @SuppressWarnings("NullAway")
        @BeforeEach
        void setUp() {
            loggingService = new LoggingService(properties, (errorResponse, exception) -> true);
        }

        @Test
        void shouldLogWithFullStacktraceForConfiguredClass(CapturedOutput output) {
            properties.setFullStacktraceClasses(List.of(IllegalArgumentException.class));
            ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "TEST", "Test message");
            IllegalArgumentException exception = new IllegalArgumentException("Test exception for configured class");

            loggingService.logException(errorResponse, exception);

            assertThat(output.getAll()).contains("Test exception for configured class");
            assertThat(output.getAll()).contains("java.lang.IllegalArgumentException");
        }

        @Test
        void shouldLogWithFullStacktraceForConfiguredHttpStatus(CapturedOutput output) {
            properties.setFullStacktraceHttpStatuses(List.of("500"));
            ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "TEST", "Test message");
            RuntimeException exception = new RuntimeException("Test exception for 500 status");

            loggingService.logException(errorResponse, exception);

            assertThat(output.getAll()).contains("Test exception for 500 status");
            assertThat(output.getAll()).contains("java.lang.RuntimeException");
        }

        @Test
        void shouldLogWithFullStacktraceForWildcardHttpStatus(CapturedOutput output) {
            properties.setFullStacktraceHttpStatuses(List.of("40x"));
            ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.NOT_FOUND, "TEST", "Test message");
            RuntimeException exception = new RuntimeException("Test exception for 40x wildcard");

            loggingService.logException(errorResponse, exception);

            assertThat(output.getAll()).contains("Test exception for 40x wildcard");
            assertThat(output.getAll()).contains("java.lang.RuntimeException");
        }

        @Test
        void shouldLogWithFullStacktraceForDoubleWildcardHttpStatus(CapturedOutput output) {
            properties.setFullStacktraceHttpStatuses(List.of("4xx"));
            ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, "TEST", "Test message");
            RuntimeException exception = new RuntimeException("Test exception for 4xx wildcard");

            loggingService.logException(errorResponse, exception);

            assertThat(output.getAll()).contains("Test exception for 4xx wildcard");
            assertThat(output.getAll()).contains("java.lang.RuntimeException");
        }

        @Test
        void shouldLogAtWarnLevelForConfiguredHttpStatus(CapturedOutput output) {
            properties.setExceptionLogging(ErrorHandlingProperties.ExceptionLogging.MESSAGE_ONLY);
            properties.setLogLevels(Map.of("400", LogLevel.WARN));
            ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, "TEST", "Test message");
            RuntimeException exception = new RuntimeException("Warn level message");

            loggingService.logException(errorResponse, exception);

            assertThat(output.getAll()).contains("WARN");
            assertThat(output.getAll()).contains("Warn level message");
        }

        @Test
        void shouldLogAtInfoLevelForWildcardHttpStatus(CapturedOutput output) {
            properties.setExceptionLogging(ErrorHandlingProperties.ExceptionLogging.MESSAGE_ONLY);
            properties.setLogLevels(Map.of("40x", LogLevel.INFO));
            ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.NOT_FOUND, "TEST", "Test message");
            RuntimeException exception = new RuntimeException("Info level message");

            loggingService.logException(errorResponse, exception);

            assertThat(output.getAll()).contains("INFO");
            assertThat(output.getAll()).contains("Info level message");
        }

        @Test
        void shouldLogAtDebugLevelForDoubleWildcardHttpStatus(CapturedOutput output) {
            properties.setExceptionLogging(ErrorHandlingProperties.ExceptionLogging.MESSAGE_ONLY);
            properties.setLogLevels(Map.of("4xx", LogLevel.DEBUG));
            ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.CONFLICT, "TEST", "Test message");
            RuntimeException exception = new RuntimeException("Debug level message");

            loggingService.logException(errorResponse, exception);

            assertThat(output.getAll()).contains("DEBUG");
            assertThat(output.getAll()).contains("Debug level message");
        }

        @Test
        void shouldNotLogWhenExceptionLoggingIsNoLogging(CapturedOutput output) {
            properties.setExceptionLogging(ErrorHandlingProperties.ExceptionLogging.NO_LOGGING);
            ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, "TEST", "Test message");
            RuntimeException exception = new RuntimeException("Should not be logged");

            loggingService.logException(errorResponse, exception);

            assertThat(output.getAll()).doesNotContain("Should not be logged");
        }

        @Test
        void shouldLogMessageOnlyWithoutStacktrace(CapturedOutput output) {
            properties.setExceptionLogging(ErrorHandlingProperties.ExceptionLogging.MESSAGE_ONLY);
            ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, "TEST", "Test message");
            RuntimeException exception = new RuntimeException("Message only test");

            loggingService.logException(errorResponse, exception);

            assertThat(output.getAll()).contains("Message only test");
            assertThat(output.getAll()).doesNotContain("at io.github.wimdeblauwe");
        }

        @Test
        void shouldLogWithStacktraceWhenConfigured(CapturedOutput output) {
            properties.setExceptionLogging(ErrorHandlingProperties.ExceptionLogging.WITH_STACKTRACE);
            ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, "TEST", "Test message");
            RuntimeException exception = new RuntimeException("Stacktrace test");

            loggingService.logException(errorResponse, exception);

            assertThat(output.getAll()).contains("Stacktrace test");
            assertThat(output.getAll()).contains("java.lang.RuntimeException");
        }

        @Test
        void shouldDefaultToErrorLevelWhenNoLogLevelConfigured(CapturedOutput output) {
            properties.setExceptionLogging(ErrorHandlingProperties.ExceptionLogging.MESSAGE_ONLY);
            ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, "TEST", "Test message");
            RuntimeException exception = new RuntimeException("Default error level");

            loggingService.logException(errorResponse, exception);

            assertThat(output.getAll()).contains("ERROR");
            assertThat(output.getAll()).contains("Default error level");
        }

        @Test
        void shouldUseExactStatusMatchBeforeWildcard(CapturedOutput output) {
            properties.setExceptionLogging(ErrorHandlingProperties.ExceptionLogging.MESSAGE_ONLY);
            properties.setLogLevels(Map.of("400", LogLevel.WARN, "4xx", LogLevel.DEBUG));
            ApiErrorResponse errorResponse = new ApiErrorResponse(HttpStatus.BAD_REQUEST, "TEST", "Test message");
            RuntimeException exception = new RuntimeException("Exact match test");

            loggingService.logException(errorResponse, exception);

            assertThat(output.getAll()).contains("WARN");
            assertThat(output.getAll()).doesNotContain("DEBUG");
        }
    }
}
