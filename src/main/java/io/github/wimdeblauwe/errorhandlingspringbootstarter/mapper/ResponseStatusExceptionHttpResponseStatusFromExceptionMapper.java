package io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class ResponseStatusExceptionHttpResponseStatusFromExceptionMapper implements HttpResponseStatusFromExceptionMapper {

    @Override
    public boolean canExtractResponseStatus(Throwable exception) {
        return exception instanceof ResponseStatusException;
    }

    @Override
    public HttpStatusCode getResponseStatus(Throwable exception) {
        return ((ResponseStatusException) exception).getStatusCode();
    }
}
