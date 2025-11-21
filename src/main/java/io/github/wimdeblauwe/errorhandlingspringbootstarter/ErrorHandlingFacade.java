package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import java.util.List;

public class ErrorHandlingFacade {

    private final List<ApiExceptionHandler> handlers;
    private final FallbackApiExceptionHandler fallbackHandler;
    private final LoggingService loggingService;
    private final List<ApiErrorResponseCustomizer> responseCustomizers;

    public ErrorHandlingFacade(List<ApiExceptionHandler> handlers, FallbackApiExceptionHandler fallbackHandler, LoggingService loggingService,
                               List<ApiErrorResponseCustomizer> responseCustomizers) {
        this.handlers = handlers;
        this.fallbackHandler = fallbackHandler;
        this.loggingService = loggingService;
        this.responseCustomizers = responseCustomizers;
    }

    public ApiErrorResponse handle(Throwable exception) {
        ApiErrorResponse errorResponse = null;
        for (ApiExceptionHandler handler : handlers) {
            if (handler.canHandle(exception)) {
                errorResponse = handler.handle(exception);
                break;
            }
        }

        if (errorResponse == null) {
            errorResponse = fallbackHandler.handle(exception);
        }

        for (ApiErrorResponseCustomizer responseCustomizer : responseCustomizers) {
            responseCustomizer.customize(errorResponse);
        }

        loggingService.logException(errorResponse, exception);

        return errorResponse;
    }
}
