package io.github.wimdeblauwe.errorhandlingspringbootstarter.reactive;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingFacade;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ProblemDetailFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.webflux.autoconfigure.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.webflux.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.Objects;

public class GlobalErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalErrorWebExceptionHandler.class);

    private final ErrorHandlingFacade errorHandlingFacade;
    private final ErrorHandlingProperties errorHandlingProperties;
    private final ProblemDetailFactory problemDetailFactory;


    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes,
                                          WebProperties.Resources resources,
                                          ErrorProperties errorProperties,
                                          ApplicationContext applicationContext,
                                          ErrorHandlingFacade errorHandlingFacade,
                                          ErrorHandlingProperties errorHandlingProperties,
                                          ProblemDetailFactory problemDetailFactory) {
        super(errorAttributes, resources, errorProperties, applicationContext);
        this.errorHandlingFacade = errorHandlingFacade;
        this.errorHandlingProperties = errorHandlingProperties;
        this.problemDetailFactory = problemDetailFactory;
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @Override
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        return handleException(request);
    }

    public Mono<ServerResponse> handleException(ServerRequest request) {
        Locale locale = request.exchange().getLocaleContext().getLocale();
        Throwable exception = getError(request);
        LOGGER.debug("webRequest: {}", request);
        LOGGER.debug("locale: {}", locale);

        ApiErrorResponse errorResponse = errorHandlingFacade.handle(Objects.requireNonNull(exception));

        if (errorHandlingProperties.isUseProblemDetailFormat()) {
            return ServerResponse.status(Objects.requireNonNull(errorResponse.getHttpStatus()))
                                 .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                                 .body(BodyInserters.fromValue(problemDetailFactory.build(errorResponse)));
        } else {
            return ServerResponse.status(Objects.requireNonNull(errorResponse.getHttpStatus()))
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .body(BodyInserters.fromValue(errorResponse));
        }
    }
}
