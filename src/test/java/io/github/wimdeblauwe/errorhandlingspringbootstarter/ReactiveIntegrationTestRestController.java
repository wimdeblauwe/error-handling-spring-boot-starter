package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.exception.ApplicationException;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.exception.ExceptionWithBadRequestStatus;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.ServerResponse;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

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

    @PostMapping
    public Mono<UserDto> createUser(@RequestBody @Valid CreateUserRequest request) {
        return Mono.just(new UserDto());
    }

    public static class UserDto {
    }

    public record CreateUserRequest(@NotBlank @NotNull String name, @Email @NotNull String email) {
    }
}
