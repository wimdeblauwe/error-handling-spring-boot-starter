package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotEmpty;

@RestController
@RequestMapping
public class ServerWebInputExceptionHandlerTestController {

    @GetMapping("/matrix-variable")
    public Mono<Void> matrixVariable(@MatrixVariable String contactNumber) {
        return Mono.empty();
    }

    @GetMapping("/request-cookie")
    public void requestCookie(@CookieValue("favorite") String favoriteCookie) {
    }

    @GetMapping("/request-header")
    public void requestHeader(@RequestHeader("X-Custom-Header") String customHeader) {
    }

    @GetMapping("/request-parameter")
    public Mono<Integer> requestParameter(@RequestParam @NotEmpty String test) {
        return Mono.just(1);
    }

}
