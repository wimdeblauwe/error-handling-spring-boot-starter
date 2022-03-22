package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.exception.ApplicationException;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.exception.ExceptionWithBadRequestStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.ServerResponse;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/integration-test")
public class ReactiveIntegrationTestRestController {

    @GetMapping("/runtime")
    Mono<Void> throwRuntimeException() {
        throw new RuntimeException("This is a test RuntimeException");
    }

    @GetMapping("/bad-request")
    Mono<String> throwExceptionWithBadRequestStatus() {
        throw new ExceptionWithBadRequestStatus();
    }

    @GetMapping("/application-request")
    Mono<String> throwApplicationException() {
        throw new ApplicationException("Application error");
    }
}
