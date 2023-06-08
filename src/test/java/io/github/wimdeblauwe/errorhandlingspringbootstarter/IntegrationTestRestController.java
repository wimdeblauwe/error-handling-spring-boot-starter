package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.exception.ExceptionWithBadRequestStatus;
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

    @GetMapping("/inherit")
    void testInherit() {
        throw new ResourceNotFoundException("test-use-case", ResourceNotFoundException.class, "my-id");
    }

    static class ApplicationError extends RuntimeException {

        @ResponseErrorProperty("code")
        protected final String useCaseCode;

        public ApplicationError(String useCaseCode, String message) {
            super(message);
            this.useCaseCode = useCaseCode;
        }
    }

    static class ResourceNotFoundException extends ApplicationError {

        public ResourceNotFoundException(String useCaseCode, Class<?> resource, String resourceId) {
            super(useCaseCode, resource.getSimpleName() + " with id " + resourceId + " not found");
        }
    }
}
