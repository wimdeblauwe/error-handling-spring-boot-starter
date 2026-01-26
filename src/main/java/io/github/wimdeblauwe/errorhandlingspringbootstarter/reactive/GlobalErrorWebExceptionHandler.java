package io.github.wimdeblauwe.errorhandlingspringbootstarter.reactive;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.webflux.autoconfigure.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.webflux.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.Objects;

public class GlobalErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalErrorWebExceptionHandler.class);

    private final ErrorHandlingFacade errorHandlingFacade;
    private final ReactiveResponseFactory responseFactory;

    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes,
                                          WebProperties.Resources resources,
                                          ErrorProperties errorProperties,
                                          ApplicationContext applicationContext,
                                          ErrorHandlingFacade errorHandlingFacade,
                                          ReactiveResponseFactory responseFactory) {
        super(errorAttributes, resources, errorProperties, applicationContext);
        this.errorHandlingFacade = errorHandlingFacade;
        this.responseFactory = responseFactory;
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

        return responseFactory.create(errorResponse);
    }
}
