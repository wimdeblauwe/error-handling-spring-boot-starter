package io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.Locale;

@ControllerAdvice(annotations = RestController.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ErrorHandlingControllerAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandlingControllerAdvice.class);

    private final ErrorHandlingFacade errorHandlingFacade;

    public ErrorHandlingControllerAdvice(ErrorHandlingFacade errorHandlingFacade) {
        this.errorHandlingFacade = errorHandlingFacade;
    }

    @ExceptionHandler
    public ResponseEntity<?> handleException(Throwable exception, WebRequest webRequest, Locale locale) {
        LOGGER.debug("webRequest: {}", webRequest);
        LOGGER.debug("locale: {}", locale);

        ApiErrorResponse errorResponse = errorHandlingFacade.handle(exception);

        return ResponseEntity.status(errorResponse.getHttpStatus())
                             .body(errorResponse);
    }
}
