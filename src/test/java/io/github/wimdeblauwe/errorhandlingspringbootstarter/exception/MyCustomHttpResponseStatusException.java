package io.github.wimdeblauwe.errorhandlingspringbootstarter.exception;

import org.springframework.http.HttpStatusCode;

public class MyCustomHttpResponseStatusException extends RuntimeException {
    private final HttpStatusCode httpStatusCode;

    public MyCustomHttpResponseStatusException(HttpStatusCode httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public HttpStatusCode getHttpStatusCode() {
        return httpStatusCode;
    }
}
