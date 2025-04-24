package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.exception.MyCustomHttpResponseStatusException;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpResponseStatusFromExceptionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = IntegrationTestRestController.class,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                "error.handling.full-stacktrace-http-statuses[0]=500"})
@Import({IntegrationTest.WebSecurityConfig.class, IntegrationTest.ResponseCustomizerConfiguration.class, IntegrationTest.CustomHttpResponseStatusFromExceptionMapper.class})
public class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testRuntimeException() throws Exception {
        mockMvc.perform(get("/integration-test/runtime"))
               .andExpect(status().isInternalServerError());
    }

    @Test
    void testExceptionWithBadRequestStatus() throws Exception {
        mockMvc.perform(get("/integration-test/bad-request"))
               .andExpect(status().isBadRequest())
               .andDo(print())
               .andExpect(jsonPath("instant").exists())
               .andExpect(jsonPath("currentApplication").value("test-app"))
        ;
    }

    @Test
    void testExceptionWithCustomStatus() throws Exception {
        mockMvc.perform(get("/integration-test/teapot"))
               .andExpect(status().isIAmATeapot())
               .andDo(print())
               .andExpect(jsonPath("instant").exists())
               .andExpect(jsonPath("currentApplication").value("test-app"))
        ;
    }

    static class WebSecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(registry -> registry.anyRequest().permitAll());
            return http.build();
        }
    }

    static class ResponseCustomizerConfiguration {
        @Bean
        public ApiErrorResponseCustomizer timestampErrorResponseCustomizer() {
            return new ApiErrorResponseCustomizer() {
                @Override
                public void customize(ApiErrorResponse response) {
                    response.addErrorProperty("instant", Instant.now());
                }
            };
        }

        @Bean
        public ApiErrorResponseCustomizer applicationErrorResponseCustomizer() {
            return new ApiErrorResponseCustomizer() {
                @Override
                public void customize(ApiErrorResponse response) {
                    response.addErrorProperty("currentApplication", "test-app");
                }
            };
        }
    }

    static class CustomHttpResponseStatusFromExceptionMapper implements HttpResponseStatusFromExceptionMapper {

        @Override
        public boolean canExtractResponseStatus(Throwable exception) {
            return exception instanceof MyCustomHttpResponseStatusException;
        }

        @Override
        public HttpStatusCode getResponseStatus(Throwable exception) {
            return ((MyCustomHttpResponseStatusException) exception).getHttpStatusCode();
        }
    }
}
