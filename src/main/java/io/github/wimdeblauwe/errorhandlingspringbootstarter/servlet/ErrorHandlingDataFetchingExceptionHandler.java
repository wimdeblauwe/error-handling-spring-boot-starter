package io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet;

import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.FallbackGraphqlExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.GraphqlExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.LoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.NestedExceptionUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ErrorHandlingDataFetchingExceptionHandler implements DataFetcherExceptionHandler {
    /**
     * Generic concrete implementation of {@link DataFetcherExceptionHandler} which globally treats all thrown exceptions
     * This Adapter instance will work for any framework which delegates error handling to graphql-java {@link DataFetcherExceptionHandler}
     */

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandlingDataFetchingExceptionHandler.class);

    private final List<GraphqlExceptionHandler> handlers;
    private final FallbackGraphqlExceptionHandler fallbackHandler;
    private final LoggingService loggingService;

    public ErrorHandlingDataFetchingExceptionHandler(List<GraphqlExceptionHandler> handlers, FallbackGraphqlExceptionHandler fallbackGraphqlExceptionHandler, LoggingService loggingService) {
        this.handlers = handlers;
        this.fallbackHandler = fallbackGraphqlExceptionHandler;
        this.loggingService = loggingService;

        LOGGER.info("GraphQL Error Handling Spring Boot Starter active with {} handlers", this.handlers.size());
        LOGGER.debug("Handlers: {}", this.handlers);
    }

    @Override
    public CompletableFuture<DataFetcherExceptionHandlerResult> handleException(DataFetcherExceptionHandlerParameters handlerParameters) {
        Throwable t = NestedExceptionUtils.getMostSpecificCause(handlerParameters.getException());
        GraphQLError graphQLError = null;

        for (GraphqlExceptionHandler handler: handlers) {
            if (handler.canHandle(t)) {
                graphQLError = handler.handle(t);
            }
        }

        if (graphQLError == null) {
            graphQLError = fallbackHandler.handle(t);
        }

        loggingService.logException(graphQLError, t);

        DataFetcherExceptionHandlerResult result = DataFetcherExceptionHandlerResult
                .newResult(graphQLError)
                .build();
        return CompletableFuture.completedFuture(result);
    }
}
