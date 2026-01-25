package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;


import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.reactive.ReactiveErrorHandlingConfiguration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.assertj.WebTestClientResponse;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(
        properties = {
                "spring.main.web-application-type=reactive",
                "spring.main.allow-bean-definition-overriding=true",
                "error.handling.full-stacktrace-http-statuses[0]=500"
        },
        controllers = ServerErrorExceptionHandlerTestController.class
)
@Import(ReactiveErrorHandlingConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ServerErrorExceptionHandlerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    @WithMockUser
    void testPathVariable() {
        WebTestClient.ResponseSpec spec = webTestClient.get()
                                                       .uri("/path-variable")
                                                       .accept(MediaType.APPLICATION_JSON)
                                                       .exchange();
        WebTestClientResponse response = WebTestClientResponse.from(spec);
        assertThat(response).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response).hasContentType(MediaType.APPLICATION_JSON);

        var bodyJson = assertThat(response).bodyJson();
        bodyJson.extractingPath("code").isEqualTo("SERVER_ERROR");
        bodyJson.extractingPath("parameterName").isEqualTo("id");
        bodyJson.extractingPath("parameterType").isEqualTo("String");
        bodyJson.extractingPath("methodName").isEqualTo("pathVariable");
        bodyJson.extractingPath("methodClassName").isEqualTo("ServerErrorExceptionHandlerTestController");
    }

    @Nested
    @TestPropertySource(properties = "error.handling.use-problem-detail-format=true")
    class ProblemDetailsFormatTests {

        @Test
        @WithMockUser
        void testProblemDetailFormat() {
            WebTestClient.ResponseSpec spec = webTestClient.get()
                                                           .uri("/path-variable")
                                                           .accept(MediaType.APPLICATION_JSON)
                                                           .exchange();
            WebTestClientResponse response = WebTestClientResponse.from(spec);
            assertThat(response).hasStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);

            var bodyJson = assertThat(response).bodyJson();

            bodyJson.extractingPath("title").isEqualTo("Internal Server Error");
            bodyJson.extractingPath("type").isEqualTo("server-error");
            bodyJson.extractingPath("detail").isEqualTo("500 INTERNAL_SERVER_ERROR \"id\"");
            bodyJson.extractingPath("parameterName").isEqualTo("id");
            bodyJson.extractingPath("parameterType").isEqualTo("String");
            bodyJson.extractingPath("methodName").isEqualTo("pathVariable");
            bodyJson.extractingPath("methodClassName").isEqualTo("ServerErrorExceptionHandlerTestController");
        }
    }
}
