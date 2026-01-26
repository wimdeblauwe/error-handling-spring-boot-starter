package io.github.wimdeblauwe.errorhandlingspringbootstarter.reactive;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ProblemDetailFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class ReactiveProblemDetailResponseFactory implements ReactiveResponseFactory {
    private final ProblemDetailFactory problemDetailFactory;

    public ReactiveProblemDetailResponseFactory(ProblemDetailFactory problemDetailFactory) {
        this.problemDetailFactory = problemDetailFactory;
    }

    @Override
    public Mono<ServerResponse> create(ApiErrorResponse errorResponse) {
        return ServerResponse.status(Objects.requireNonNull(errorResponse.getHttpStatus()))
                             .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                             .body(BodyInserters.fromValue(problemDetailFactory.build(errorResponse)));
    }
}
