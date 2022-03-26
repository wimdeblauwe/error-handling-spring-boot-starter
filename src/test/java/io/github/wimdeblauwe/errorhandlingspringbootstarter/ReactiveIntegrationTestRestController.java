package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.exception.ApplicationException;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.exception.ExceptionWithBadRequestStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.ServerResponse;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

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

    public static class CreateUserRequest {
        @NotBlank
        private String name;
        @Email
        private String email;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
