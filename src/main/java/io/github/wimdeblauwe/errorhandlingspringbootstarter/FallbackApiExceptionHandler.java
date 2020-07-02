package io.github.wimdeblauwe.errorhandlingspringbootstarter;

public interface FallbackApiExceptionHandler {
    ApiErrorResponse handle(Throwable exception);
}
