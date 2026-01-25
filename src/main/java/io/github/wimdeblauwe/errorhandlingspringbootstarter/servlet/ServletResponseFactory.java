package io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ResponseFactory;
import org.springframework.http.ResponseEntity;

/**
 * A specialized {@link ResponseFactory} interface for creating {@link ResponseEntity} objects
 * from instances of {@link io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse ApiErrorResponse} within the context of a servlet-based web application.
 * <p>
 * Implementations of this interface define how <code>ApiErrorResponse</code> objects should be transformed
 * into {@link ResponseEntity} objects to work seamlessly with servlet environments in Spring applications.
 */
public interface ServletResponseFactory extends ResponseFactory<ResponseEntity<?>> {
}
