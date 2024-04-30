package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import graphql.GraphQLError;

public interface FallbackGraphqlExceptionHandler {

    GraphQLError handle(Throwable exception);
}
