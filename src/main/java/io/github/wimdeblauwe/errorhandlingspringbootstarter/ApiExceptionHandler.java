package io.github.wimdeblauwe.errorhandlingspringbootstarter;

public interface ApiExceptionHandler {
    boolean canHandle(Throwable exception);

    ApiErrorResponse handle(Throwable exception);
}
