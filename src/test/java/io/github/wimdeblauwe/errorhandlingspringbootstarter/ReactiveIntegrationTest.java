package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.reactive.ReactiveErrorHandlingConfiguration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(
        properties = {
                "spring.main.web-application-type=reactive",
                "spring.main.allow-bean-definition-overriding=true",
                "error.handling.full-stacktrace-http-statuses[0]=500"
        },
        controllers = ReactiveIntegrationTestRestController.class
)
@ImportAutoConfiguration(classes = {ReactiveErrorHandlingConfiguration.class, ReactiveIntegrationTest.CodecConfig.class,})
public class ReactiveIntegrationTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    @WithMockUser
    void testRuntimeException() throws Exception {
        webTestClient.get()
                     .uri("/integration-test/runtime")
                     .accept(MediaType.ALL)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @WithMockUser
    void testExceptionWithBadRequestStatus() throws Exception {
        webTestClient.get()
                     .uri("/integration-test/bad-request")
                     .exchange()
                     .expectStatus().isBadRequest();
    }

    @Test
    @WithMockUser
    void testApplicationException() throws Exception {
        webTestClient.get()
                     .uri("/integration-test/application-request")
                     .exchange()
                     .expectStatus().is5xxServerError()
                     .expectBody()
                     .jsonPath("$.code").isEqualTo("APPLICATION")
                     .jsonPath("$.message").isEqualTo("Application error");
    }

    @Test
    @WithMockUser
    void testPostWithValidationError() {
        webTestClient.mutateWith(csrf())
                     .post()
                     .uri("/integration-test")
                     .contentType(MediaType.APPLICATION_JSON)
                     .bodyValue("{\n" +
                                        "  \"name\": \"\",\n" +
                                        "  \"email\": \"invalid\"\n" +
                                        "}")
                     .exchange()
                     .expectStatus().isBadRequest()
                     .expectBody()
                     .jsonPath("$.code").isEqualTo("VALIDATION_FAILED")
                     .jsonPath("$.message").isEqualTo("Validation failed for object='createUserRequest'. Error count: 2")
                     .jsonPath("$.fieldErrors").isArray()
                     .jsonPath("$.fieldErrors..code").value(Matchers.containsInAnyOrder("INVALID_EMAIL", "REQUIRED_NOT_BLANK"))
                     .jsonPath("$.fieldErrors..message").value(Matchers.containsInAnyOrder("must be a well-formed email address", "must not be blank"))
                     .jsonPath("$.fieldErrors..property").value(Matchers.containsInAnyOrder("email", "name"))
                     .jsonPath("$.fieldErrors..rejectedValue").value(Matchers.containsInAnyOrder("invalid", ""));
    }

    static class CodecConfig {

        @Bean
        @ConditionalOnMissingBean(ServerCodecConfigurer.class)
        public ServerCodecConfigurer serverCodecConfigurer() {
            return ServerCodecConfigurer.create();
        }
    }

}
