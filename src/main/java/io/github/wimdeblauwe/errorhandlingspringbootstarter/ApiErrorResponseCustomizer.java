package io.github.wimdeblauwe.errorhandlingspringbootstarter;

public interface ApiErrorResponseCustomizer {
    void customize(ApiErrorResponse response);
}
