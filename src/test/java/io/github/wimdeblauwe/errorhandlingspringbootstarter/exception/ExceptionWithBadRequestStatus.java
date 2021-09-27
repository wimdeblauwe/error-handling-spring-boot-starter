package io.github.wimdeblauwe.errorhandlingspringbootstarter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ExceptionWithBadRequestStatus extends RuntimeException {

}
