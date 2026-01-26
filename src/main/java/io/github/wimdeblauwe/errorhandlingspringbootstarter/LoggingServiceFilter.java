package io.github.wimdeblauwe.errorhandlingspringbootstarter;

/**
 * Interface for deciding whether an exception should be logged.
 */
public interface LoggingServiceFilter {

    boolean shouldLogException(ApiErrorResponse errorResponse, Throwable exception);
}
