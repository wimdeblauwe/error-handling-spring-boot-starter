package io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.Locale;
import java.util.Objects;

@ControllerAdvice(annotations = RestController.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ErrorHandlingControllerAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandlingControllerAdvice.class);

    private final ErrorHandlingFacade errorHandlingFacade;
    private final ErrorHandlingProperties errorHandlingProperties;
    private final ProblemDetailFactory problemDetailFactory;

    public ErrorHandlingControllerAdvice(ErrorHandlingFacade errorHandlingFacade, ErrorHandlingProperties errorHandlingProperties, ProblemDetailFactory problemDetailFactory) {
        this.errorHandlingFacade = errorHandlingFacade;
        this.errorHandlingProperties = errorHandlingProperties;
        this.problemDetailFactory = problemDetailFactory;
    }

    @ExceptionHandler
    public ResponseEntity<?> handleException(Throwable exception, WebRequest webRequest, Locale locale) {
        LOGGER.debug("webRequest: {}", webRequest);
        LOGGER.debug("locale: {}", locale);

        ApiErrorResponse errorResponse = errorHandlingFacade.handle(exception);

        if (errorHandlingProperties.isUseProblemDetailFormat()) {
            ProblemDetail problemDetail = problemDetailFactory.build(errorResponse);
            return ResponseEntity.status(Objects.requireNonNull(errorResponse.getHttpStatus()))
                                 .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                                 .body(problemDetail);
        } else {
            return ResponseEntity.status(Objects.requireNonNull(errorResponse.getHttpStatus()))
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .body(errorResponse);
        }
    }
}
