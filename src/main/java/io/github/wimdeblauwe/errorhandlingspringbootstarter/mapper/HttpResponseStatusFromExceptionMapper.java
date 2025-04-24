package io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper;

import org.springframework.http.HttpStatusCode;

/**
 * This interface can be used to contribute Spring beans that can extract
 * a {@link HttpStatusCode} from the exception instance.
 */
public interface HttpResponseStatusFromExceptionMapper {
    /**
     * Determine if this {@link HttpResponseStatusFromExceptionMapper} can extract
     * a {@link HttpStatusCode} from the exception instance.
     * It is guaranteed that this method is called first, and the {@link #getResponseStatus(Throwable)} method
     * will only be called if this method returns <code>true</code>.
     *
     * @param exception the Throwable that needs to be examined
     * @return true if this mapper can extract the response status, false otherwise
     */
    boolean canExtractResponseStatus(Throwable exception);

    /**
     * Extract a {@link HttpStatusCode} from the exception instance.
     *
     * @param exception the Throwable that was thrown
     * @return the non-null <code>HttpStatusCode</code>.
     */
    HttpStatusCode getResponseStatus(Throwable exception);
}
