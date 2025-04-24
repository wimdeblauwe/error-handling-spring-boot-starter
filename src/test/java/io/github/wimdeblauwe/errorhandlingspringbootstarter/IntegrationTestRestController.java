package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.exception.ExceptionWithBadRequestStatus;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.exception.MyCustomHttpResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/integration-test")
public class IntegrationTestRestController {

    @GetMapping("/runtime")
    void throwRuntimeException() {
        throw new RuntimeException("This is a test RuntimeException");
    }

    @GetMapping("/bad-request")
    void throwExceptionWithBadRequestStatus() {
        throw new ExceptionWithBadRequestStatus();
    }

    @GetMapping("/teapot")
    void throwMyCustomHttpResponseStatusException() {
        throw new MyCustomHttpResponseStatusException(HttpStatus.I_AM_A_TEAPOT);
    }
}
