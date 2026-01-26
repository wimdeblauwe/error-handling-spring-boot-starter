package io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ProblemDetailFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

class ServletProblemDetailResponseFactory implements ServletResponseFactory {

    private final ProblemDetailFactory problemDetailFactory;

    public ServletProblemDetailResponseFactory(ProblemDetailFactory problemDetailFactory) {
        this.problemDetailFactory = problemDetailFactory;
    }

    @Override
    public ResponseEntity<?> create(ApiErrorResponse errorResponse) {
        ProblemDetail problemDetail = problemDetailFactory.build(errorResponse);
        return ResponseEntity.status(Objects.requireNonNull(errorResponse.getHttpStatus()))
                             .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                             .body(problemDetail);
    }

}
