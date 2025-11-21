package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;


import io.github.wimdeblauwe.errorhandlingspringbootstarter.reactive.ReactiveErrorHandlingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.assertj.WebTestClientResponse;

import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(
        properties = {
                "spring.main.web-application-type=reactive",
                "spring.main.allow-bean-definition-overriding=true",
                "error.handling.full-stacktrace-http-statuses[0]=500"
        },
        controllers = ServerWebInputExceptionHandlerTestController.class
)
@Import(ReactiveErrorHandlingConfiguration.class)
class ServerWebInputExceptionHandlerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    @WithMockUser
    void testMatrixVariable() {
        WebTestClient.ResponseSpec spec = webTestClient.get()
                                                       .uri("/matrix-variable")
                                                       .accept(MediaType.APPLICATION_JSON)
                                                       .exchange();
        WebTestClientResponse response = WebTestClientResponse.from(spec);
        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);

        var bodyJson = assertThat(response).bodyJson();
        bodyJson.extractingPath("code").isEqualTo("VALIDATION_FAILED");
        bodyJson.extractingPath("parameterName").isEqualTo("contactNumber");
        bodyJson.extractingPath("parameterType").isEqualTo("String");
    }

    @Test
    @WithMockUser
    void testRequestCookie() {
        WebTestClient.ResponseSpec spec = webTestClient.get()
                                                       .uri("/request-cookie")
                                                       .accept(MediaType.APPLICATION_JSON)
                                                       .exchange();
        WebTestClientResponse response = WebTestClientResponse.from(spec);
        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);

        var bodyJson = assertThat(response).bodyJson();
        bodyJson.extractingPath("code").isEqualTo("VALIDATION_FAILED");
        bodyJson.extractingPath("message").isEqualTo("400 BAD_REQUEST \"Required cookie 'favorite' is not present.\"");
        bodyJson.extractingPath("parameterName").isEqualTo("favoriteCookie");
        bodyJson.extractingPath("parameterType").isEqualTo("String");
    }

    @Test
    @WithMockUser
    void testRequestHeader() {
        WebTestClient.ResponseSpec spec = webTestClient.get()
                                                       .uri("/request-header")
                                                       .accept(MediaType.APPLICATION_JSON)
                                                       .exchange();
        WebTestClientResponse response = WebTestClientResponse.from(spec);
        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);

        var bodyJson = assertThat(response).bodyJson();
        bodyJson.extractingPath("code").isEqualTo("VALIDATION_FAILED");
        bodyJson.extractingPath("message").isEqualTo("400 BAD_REQUEST \"Required header 'X-Custom-Header' is not present.\"");
        bodyJson.extractingPath("parameterName").isEqualTo("customHeader");
        bodyJson.extractingPath("parameterType").isEqualTo("String");
    }

    @Test
    @WithMockUser
    void testRequestParameter() {
        WebTestClient.ResponseSpec spec = webTestClient.get()
                                                       .uri("/request-parameter")
                                                       .accept(MediaType.APPLICATION_JSON)
                                                       .exchange();

        WebTestClientResponse response = WebTestClientResponse.from(spec);
        assertThat(response).hasStatus(HttpStatus.BAD_REQUEST);

        var bodyJson = assertThat(response).bodyJson();
        bodyJson.extractingPath("code").isEqualTo("VALIDATION_FAILED");
        bodyJson.extractingPath("message").isEqualTo("400 BAD_REQUEST \"Required query parameter 'test' is not present.\"");
        bodyJson.extractingPath("parameterName").isEqualTo("test");
        bodyJson.extractingPath("parameterType").isEqualTo("String");
    }
}
