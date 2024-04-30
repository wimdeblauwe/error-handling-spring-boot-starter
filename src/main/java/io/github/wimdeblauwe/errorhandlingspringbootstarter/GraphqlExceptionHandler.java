package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import graphql.GraphQLError;

public interface GraphqlExceptionHandler {
    /**
     * Determine if this {@link GraphqlExceptionHandler} can handle the given {@link Throwable}.
     * It is guaranteed that this method is called first, and the {@link #handle(Throwable)} method
     * will only be called if this method returns <code>true</code>.
     *
     * @param exception the Throwable that needs to be handled
     * @return true if this handler can handle the Throwable, false otherwise.
     */
    boolean canHandle(Throwable exception);

    /**
     * Handle the given {@link Throwable} and return an {@link GraphQLError} instance
     * that will be serialized to JSON and returned from the controller resolver method that has
     * thrown the Throwable.
     *
     * @param exception the Throwable that needs to be handled
     * @return the non-null GraphQLError
     */
    GraphQLError handle(Throwable exception);
}
