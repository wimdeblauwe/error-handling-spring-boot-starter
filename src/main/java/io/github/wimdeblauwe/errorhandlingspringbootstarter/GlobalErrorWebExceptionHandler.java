package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GlobalErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalErrorWebExceptionHandler.class);

    private final ErrorHandlingProperties properties;
    private final List<ApiExceptionHandler> handlers;
    private final FallbackApiExceptionHandler fallbackHandler;

    /**
     * Create a new {@code DefaultErrorWebExceptionHandler} instance.
     *
     * @param errorAttributes    the error attributes
     * @param resources          the resources configuration properties
     * @param errorProperties    the error configuration properties
     * @param applicationContext the current application context
     * @param properties
     * @param handlers
     * @param fallbackHandler
     * @since 2.4.0
     */
    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes,
                                          WebProperties.Resources resources,
                                          ErrorProperties errorProperties,
                                          ApplicationContext applicationContext,
                                          ErrorHandlingProperties properties,
                                          List<ApiExceptionHandler> handlers,
                                          FallbackApiExceptionHandler fallbackHandler
    ) {
        super(errorAttributes, resources, errorProperties, applicationContext);
        this.properties = properties;
        this.handlers = handlers;
        this.fallbackHandler = fallbackHandler;
    }

    // constructors

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @Override
    protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
//        Map<String, Object> errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults());
//        Map<String, Object> errorPropertiesMap = handleException(request);
//        return ServerResponse.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(errorPropertiesMap));
        return handleException(request);
    }

    public Mono<ServerResponse> handleException(ServerRequest request) {
        Locale locale = request.exchange().getLocaleContext().getLocale();
        Throwable exception = getError(request);
        LOGGER.debug("webRequest: {}", request);
        LOGGER.debug("locale: {}", locale);

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

        Map<String, Object> mapResponse = new HashMap<>();
        mapResponse.put("code", errorResponse.getCode());
        mapResponse.put("message", errorResponse.getMessage());
        List<ApiGlobalError> globalErrors = errorResponse.getGlobalErrors();
        if(!globalErrors.isEmpty()) {
            mapResponse.put("globalErrors", globalErrors);
        }
        List<ApiFieldError> fieldErrors = errorResponse.getFieldErrors();
        if(!fieldErrors.isEmpty()) {
            mapResponse.put("fieldErrors", fieldErrors);
        }
        List<ApiParameterError> parameterErrors = errorResponse.getParameterErrors();
        if(!parameterErrors.isEmpty()) {
            mapResponse.put("paramaterErrors", parameterErrors);
        }
        Map<String, Object> properties = errorResponse.getProperties();
        if(!properties.isEmpty()) {
            mapResponse.put("propertes", properties);
        }
        logException(errorResponse, exception);
        return ServerResponse.status(errorResponse.getHttpStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(mapResponse));
    }

    private void logException(ApiErrorResponse errorResponse, Throwable exception) {
        if (properties.getFullStacktraceClasses().contains(exception.getClass())) {
            LOGGER.error(exception.getMessage(), exception);
        } else if (!properties.getFullStacktraceHttpStatuses().isEmpty()) {
            boolean alreadyLogged = logFullStacktraceIfNeeded(errorResponse.getHttpStatus(), exception);
            if (!alreadyLogged) {
                doStandardFallbackLogging(exception);
            }
        } else {
            doStandardFallbackLogging(exception);
        }
    }

    private void doStandardFallbackLogging(Throwable exception) {
        switch (properties.getExceptionLogging()) {
            case WITH_STACKTRACE:
                LOGGER.error(exception.getMessage(), exception);
                break;
            case MESSAGE_ONLY:
                LOGGER.error(exception.getMessage());
                break;
        }
    }

    private boolean logFullStacktraceIfNeeded(HttpStatus httpStatus, Throwable exception) {
        String httpStatusValue = String.valueOf(httpStatus.value());
        if (properties.getFullStacktraceHttpStatuses().contains(httpStatusValue)) {
            LOGGER.error(exception.getMessage(), exception);
            return true;
        } else if (properties.getFullStacktraceHttpStatuses().contains(httpStatusValue.replaceFirst("\\d$", "x"))) {
            LOGGER.error(exception.getMessage(), exception);
            return true;
        } else if (properties.getFullStacktraceHttpStatuses().contains(httpStatusValue.replaceFirst("\\d\\d$", "xx"))) {
            LOGGER.error(exception.getMessage(), exception);
            return true;
        }

        return false;
    }

}
