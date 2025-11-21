package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.reactive.ReactiveErrorHandlingConfiguration;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.assertj.WebTestClientResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WebFluxTest(
        properties = {
                "spring.main.web-application-type=reactive",
                "spring.main.allow-bean-definition-overriding=true",
                "error.handling.full-stacktrace-http-statuses[0]=500"
        },
        controllers = ReactiveIntegrationTestRestController.class
)
@Import(ReactiveErrorHandlingConfiguration.class)
public class ReactiveIntegrationTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    @WithMockUser
    void testRuntimeException() {
        webTestClient.get()
                     .uri("/integration-test/runtime")
                     .accept(MediaType.ALL)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @WithMockUser
    void testExceptionWithBadRequestStatus() {
        webTestClient.get()
                     .uri("/integration-test/bad-request")
                     .exchange()
                     .expectStatus().isBadRequest();
    }

    @Test
    @WithMockUser
    void testApplicationException() {
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
        WebTestClient.ResponseSpec spec = webTestClient.mutateWith(csrf())
                                                           .post()
                                                           .uri("/integration-test")
                                                           .contentType(MediaType.APPLICATION_JSON)
                                                           .bodyValue("""
                                                                              {
                                                                                "name": "",
                                                                                "email": "invalid"
                                                                              }""")
                                                           .exchange();

        WebTestClientResponse response = WebTestClientResponse.from(spec);
        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(response).hasContentType(MediaType.APPLICATION_JSON);

        var bodyJson = assertThat(response).bodyJson();
        bodyJson.extractingPath("$.code").isEqualTo("VALIDATION_FAILED");
        bodyJson.extractingPath("$.message").isEqualTo("Validation failed for object='createUserRequest'. Error count: 2");
        bodyJson.extractingPath("$.fieldErrors").isInstanceOf(List.class);
        bodyJson.extractingPath("$.fieldErrors..code").convertTo(InstanceOfAssertFactories.list(String.class)).containsExactlyInAnyOrder("INVALID_EMAIL", "REQUIRED_NOT_BLANK");
        bodyJson.extractingPath("$.fieldErrors..message").convertTo(InstanceOfAssertFactories.list(String.class)).containsExactlyInAnyOrder("must be a well-formed email address", "must not be blank");
        bodyJson.extractingPath("$.fieldErrors..property").convertTo(InstanceOfAssertFactories.list(String.class)).containsExactlyInAnyOrder("email", "name");
        bodyJson.extractingPath("$.fieldErrors..rejectedValue").convertTo(InstanceOfAssertFactories.list(String.class)).containsExactlyInAnyOrder("invalid", "");
    }
}
