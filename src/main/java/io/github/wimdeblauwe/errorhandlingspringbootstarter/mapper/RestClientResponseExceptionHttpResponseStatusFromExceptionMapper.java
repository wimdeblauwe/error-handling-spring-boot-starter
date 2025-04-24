package io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClientResponseException;

public class RestClientResponseExceptionHttpResponseStatusFromExceptionMapper implements HttpResponseStatusFromExceptionMapper {
    @Override
    public boolean canExtractResponseStatus(Throwable exception) {
        return exception instanceof RestClientResponseException;
    }

    @Override
    public HttpStatusCode getResponseStatus(Throwable exception) {
        return ((RestClientResponseException) exception).getStatusCode();
    }
}
