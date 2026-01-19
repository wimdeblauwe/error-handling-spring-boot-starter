package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.exception.ExceptionWithBadRequestStatus;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.exception.MyCustomHttpResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

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

    @GetMapping(value = "sse", produces = "text/event-stream")
    SseEmitter sseEndpoint() throws IOException {
        SseEmitter emitter = new SseEmitter();
        emitter.send("test");

        // Simulate error during stream processing
        new Thread(() -> {
            try {
                Thread.sleep(10);
                emitter.completeWithError(new RuntimeException("Simulated SSE error"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        return emitter;
    }
}
