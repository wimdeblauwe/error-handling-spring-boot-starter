package io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

class ServletDefaultResponseFactory implements ServletResponseFactory {

    @Override
    public ResponseEntity<ApiErrorResponse> create(ApiErrorResponse errorResponse) {
        return ResponseEntity.status(Objects.requireNonNull(errorResponse.getHttpStatus()))
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(errorResponse);
    }
}
