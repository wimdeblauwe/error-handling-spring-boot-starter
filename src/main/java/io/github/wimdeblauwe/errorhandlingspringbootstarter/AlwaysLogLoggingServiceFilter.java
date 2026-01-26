package io.github.wimdeblauwe.errorhandlingspringbootstarter;

public class AlwaysLogLoggingServiceFilter implements LoggingServiceFilter {
    @Override
    public boolean shouldLogException(ApiErrorResponse errorResponse,
                                      Throwable exception) {
        return true;
    }
}
