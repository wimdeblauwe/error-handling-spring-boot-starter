package io.github.wimdeblauwe.errorhandlingspringbootstarter.exception;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ResponseErrorCode;

@ResponseErrorCode("MY_ERROR_CODE")
public class ExceptionWithResponseErrorCode extends RuntimeException {

}
