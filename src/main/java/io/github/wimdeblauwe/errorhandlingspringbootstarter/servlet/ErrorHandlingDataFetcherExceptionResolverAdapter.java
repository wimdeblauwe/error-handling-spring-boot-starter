package io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet;

import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.FallbackGraphqlExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.GraphqlExceptionHandler;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.LoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.graphql.GraphQlAutoConfiguration;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;

import java.util.List;

@ConditionalOnClass(GraphQlAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ErrorHandlingDataFetcherExceptionResolverAdapter extends DataFetcherExceptionResolverAdapter {
    /**
     * Generic concrete implementation of {@link DataFetcherExceptionResolverAdapter} which globally treats all thrown exceptions
     * This Adapter instance will work for spring-graphql based applications
     */

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandlingDataFetcherExceptionResolverAdapter.class);
    private final List<GraphqlExceptionHandler> handlers;
    private final FallbackGraphqlExceptionHandler fallbackHandler;
    private final LoggingService loggingService;

    public ErrorHandlingDataFetcherExceptionResolverAdapter(List<GraphqlExceptionHandler> handlers,
                                                            FallbackGraphqlExceptionHandler fallbackHandler,
                                                            LoggingService loggingService) {
        this.handlers = handlers;
        this.fallbackHandler = fallbackHandler;
        this.loggingService = loggingService;
        LOGGER.info("GraphQL Error Handling Spring Boot Starter active with {} handlers", this.handlers.size());
        LOGGER.debug("Handlers: {}", this.handlers);
    }

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        Throwable t = NestedExceptionUtils.getMostSpecificCause(ex);
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

        return graphQLError;
    }
}
