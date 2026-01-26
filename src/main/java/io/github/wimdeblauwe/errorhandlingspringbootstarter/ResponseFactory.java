package io.github.wimdeblauwe.errorhandlingspringbootstarter;

/**
 * Factory interface for creating a response object of type T based on an {@link ApiErrorResponse}.
 * <p>
 * Implementations of this interface are responsible for transforming the given {@link ApiErrorResponse}
 * into a response object suitable for the framework or context in which it is used.
 *
 * @param <T> the type of the response object to be created
 */
public interface ResponseFactory<T> {
    T create(ApiErrorResponse apiErrorResponse);
}
