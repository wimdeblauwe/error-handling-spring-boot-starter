package io.github.wimdeblauwe.errorhandlingspringbootstarter.reactive;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponseCustomizer;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Locale;

public class GlobalErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalErrorWebExceptionHandler.class);

    private final ErrorHandlingFacade errorHandlingFacade;

    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes,
                                          WebProperties.Resources resources,
                                          ErrorProperties errorProperties,
                                          ApplicationContext applicationContext,
                                          ErrorHandlingFacade errorHandlingFacade) {
        super(errorAttributes, resources, errorProperties, applicationContext);
        this.errorHandlingFacade = errorHandlingFacade;
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

        ApiErrorResponse errorResponse = errorHandlingFacade.handle(exception);

        return ServerResponse.status(errorResponse.getHttpStatus())
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(BodyInserters.fromValue(errorResponse));
    }
}
