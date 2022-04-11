package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;


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

@WebFluxTest(
        properties = {
                "spring.main.web-application-type=reactive",
                "spring.main.allow-bean-definition-overriding=true",
                "error.handling.full-stacktrace-http-statuses[0]=500"
        },
        controllers = ServerErrorExceptionHandlerTestController.class
)
@ImportAutoConfiguration(classes = {ReactiveErrorHandlingConfiguration.class, ServerErrorExceptionHandlerTest.CodecConfig.class})
class ServerErrorExceptionHandlerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    @WithMockUser
    void testPathVariable() throws Exception {
        webTestClient.get()
                     .uri("/path-variable")
                     .accept(MediaType.APPLICATION_JSON)
                     .exchange()
                     .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
                     .expectBody()
                     .consumeWith(System.out::println)
                     .jsonPath("code").value(Matchers.equalTo("SERVER_ERROR"))
                     .jsonPath("parameterName").value(Matchers.equalTo("id"))
                     .jsonPath("parameterType").value(Matchers.equalTo("String"))
                     .jsonPath("methodName").value(Matchers.equalTo("pathVariable"))
                     .jsonPath("methodClassName").value(Matchers.equalTo("ServerErrorExceptionHandlerTestController"))
        ;
    }

    static class CodecConfig {

        @Bean
        @ConditionalOnMissingBean(ServerCodecConfigurer.class)
        public ServerCodecConfigurer serverCodecConfigurer() {
            return ServerCodecConfigurer.create();
        }
    }

}
