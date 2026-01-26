package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;


import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.reactive.ReactiveErrorHandlingConfiguration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(
        properties = {
                "spring.main.web-application-type=reactive",
                "spring.main.allow-bean-definition-overriding=true",
                "error.handling.full-stacktrace-http-statuses[0]=500",
                "error.handling.use-problem-detail-format=true"
        },
        controllers = ServerErrorExceptionHandlerTestController.class
)
@Import(ReactiveErrorHandlingConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ServerErrorExceptionHandlerWithProblemDetailTest {

    @Autowired
    WebTestClient webTestClient;


    @Test
    @WithMockUser
    void testProblemDetailFormat() {
        webTestClient.get()
                     .uri("/path-variable")
                     .accept(MediaType.APPLICATION_JSON)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                     .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                     .expectBody()
                     .consumeWith(System.out::println)
                     .jsonPath("title").value(Matchers.equalTo("Internal Server Error"))
                     .jsonPath("type").value(Matchers.equalTo("server-error"))
                     .jsonPath("detail").value(Matchers.equalTo("500 INTERNAL_SERVER_ERROR \"id\""))
                     .jsonPath("parameterName").value(Matchers.equalTo("id"))
                     .jsonPath("parameterType").value(Matchers.equalTo("String"))
                     .jsonPath("methodName").value(Matchers.equalTo("pathVariable"))
                     .jsonPath("methodClassName").value(Matchers.equalTo("ServerErrorExceptionHandlerTestController"));
    }

    @Test
    @WithMockUser
    void testProblemDetailFormatWithoutKebabCase(@Autowired ErrorHandlingProperties properties) {
        properties.setProblemDetailConvertToKebabCase(false);
        webTestClient.get()
                     .uri("/path-variable")
                     .accept(MediaType.APPLICATION_JSON)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                     .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                     .expectBody()
                     .consumeWith(System.out::println)
                     .jsonPath("title").value(Matchers.equalTo("Internal Server Error"))
                     .jsonPath("type").value(Matchers.equalTo("SERVER_ERROR"))
                     .jsonPath("detail").value(Matchers.equalTo("500 INTERNAL_SERVER_ERROR \"id\""))
                     .jsonPath("parameterName").value(Matchers.equalTo("id"))
                     .jsonPath("parameterType").value(Matchers.equalTo("String"))
                     .jsonPath("methodName").value(Matchers.equalTo("pathVariable"))
                     .jsonPath("methodClassName").value(Matchers.equalTo("ServerErrorExceptionHandlerTestController"));
    }

}
