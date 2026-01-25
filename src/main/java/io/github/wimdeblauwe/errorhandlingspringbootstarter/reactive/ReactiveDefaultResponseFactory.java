package io.github.wimdeblauwe.errorhandlingspringbootstarter.reactive;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class ReactiveDefaultResponseFactory implements ReactiveResponseFactory {
    @Override
    public Mono<ServerResponse> create(ApiErrorResponse errorResponse) {
        return ServerResponse.status(Objects.requireNonNull(errorResponse.getHttpStatus()))
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(BodyInserters.fromValue(errorResponse));
    }
}
