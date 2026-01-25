package io.github.wimdeblauwe.errorhandlingspringbootstarter.reactive;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ResponseFactory;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * ReactiveResponseFactory is a specialized factory interface for creating reactive responses
 * using the Project Reactor {@link Mono} type and Spring WebFlux's {@link ServerResponse}.
 * <p>
 * Implementations of this interface are responsible for transforming an {@link io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse ApiErrorResponse}
 * into a reactive response suitable for handling WebFlux-based server errors.
 */
public interface ReactiveResponseFactory extends ResponseFactory<Mono<ServerResponse>> {
}
