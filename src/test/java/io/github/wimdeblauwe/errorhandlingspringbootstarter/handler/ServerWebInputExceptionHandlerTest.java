package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;


import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(
        properties = {
                "spring.main.web-application-type=reactive",
                "spring.main.allow-bean-definition-overriding=true",
                "error.handling.full-stacktrace-http-statuses[0]=500"
        },
        controllers = ServerWebInputExceptionHandlerTestController.class
)
class ServerWebInputExceptionHandlerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    @WithMockUser
    void testMatrixVariable() throws Exception {
        webTestClient.get()
                     .uri("/matrix-variable")
                     .accept(MediaType.APPLICATION_JSON)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                     .expectBody()
                     .jsonPath("code").value(Matchers.equalTo("VALIDATION_FAILED"))
                     .jsonPath("parameterName").value(Matchers.equalTo("contactNumber"))
                     .jsonPath("parameterType").value(Matchers.equalTo("String"))
        ;
    }

    @Test
    @WithMockUser
    void testRequestCookie() throws Exception {
        webTestClient.get()
                     .uri("/request-cookie")
                     .accept(MediaType.APPLICATION_JSON)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                     .expectBody()
                     .consumeWith(System.out::println)
                     .jsonPath("code").value(Matchers.equalTo("VALIDATION_FAILED"))
                     .jsonPath("message").value(Matchers.equalTo("400 BAD_REQUEST \"Missing cookie 'favorite' for method parameter of type String\""))
                     .jsonPath("parameterName").value(Matchers.equalTo("favoriteCookie"))
                     .jsonPath("parameterType").value(Matchers.equalTo("String"))
        ;
    }

    @Test
    @WithMockUser
    void testRequestHeader() throws Exception {
        webTestClient.get()
                     .uri("/request-header")
                     .accept(MediaType.APPLICATION_JSON)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                     .expectBody()
                     .consumeWith(System.out::println)
                     .jsonPath("code").value(Matchers.equalTo("VALIDATION_FAILED"))
                     .jsonPath("message").value(Matchers.equalTo("400 BAD_REQUEST \"Missing request header 'X-Custom-Header' for method parameter of type String\""))
                     .jsonPath("parameterName").value(Matchers.equalTo("customHeader"))
                     .jsonPath("parameterType").value(Matchers.equalTo("String"))
        ;
    }

    @Test
    @WithMockUser
    void testRequestParameter() throws Exception {
        webTestClient.get()
                     .uri("/request-parameter")
                     .accept(MediaType.APPLICATION_JSON)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
                     .expectBody()
                     .consumeWith(System.out::println)
                     .jsonPath("code").value(Matchers.equalTo("VALIDATION_FAILED"))
                     .jsonPath("message").value(Matchers.equalTo("400 BAD_REQUEST \"Required String parameter 'test' is not present\""))
                     .jsonPath("parameterName").value(Matchers.equalTo("test"))
                     .jsonPath("parameterType").value(Matchers.equalTo("String"));
    }
}
