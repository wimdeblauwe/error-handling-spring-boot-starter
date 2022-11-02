package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.UnauthorizedEntryPoint;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet.ServletErrorHandlingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {ServletErrorHandlingConfiguration.class,
        SpringSecurityApiExceptionHandlerTest.TestController.class,
        SpringSecurityApiExceptionHandlerTest.TestConfig.class})
class SpringSecurityApiExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testUnauthorized() throws Exception {
        mockMvc.perform(get("/test/spring-security/access-denied"))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("code").value("UNAUTHORIZED"))
               .andExpect(jsonPath("message").value("Full authentication is required to access this resource"));
    }

    @Test
    @WithMockUser
    void testAccessDenied() throws Exception {
        mockMvc.perform(get("/test/spring-security/access-denied"))
               .andExpect(status().isForbidden())
               .andExpect(jsonPath("code").value("ACCESS_DENIED"))
               .andExpect(jsonPath("message").value("Fake access denied"))
        ;
    }

    @Test
    @WithMockUser
    void testAccountExpired() throws Exception {
        mockMvc.perform(get("/test/spring-security/account-expired"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("ACCOUNT_EXPIRED"))
               .andExpect(jsonPath("message").value("Fake account expired"))
        ;
    }

    @RestController
    @RequestMapping("/test/spring-security")
    public static class TestController {

        @GetMapping("/access-denied")
        public void throwAccessDenied() {
            throw new AccessDeniedException("Fake access denied");
        }

        @GetMapping("/account-expired")
        public void throwAccountExpired() {
            throw new AccountExpiredException("Fake account expired");
        }
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public UnauthorizedEntryPoint unauthorizedEntryPoint(HttpStatusMapper httpStatusMapper, ErrorCodeMapper errorCodeMapper, ErrorMessageMapper errorMessageMapper, ObjectMapper objectMapper) {
            return new UnauthorizedEntryPoint(httpStatusMapper, errorCodeMapper, errorMessageMapper, objectMapper);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                       UnauthorizedEntryPoint unauthorizedEntryPoint) throws Exception {
            http.httpBasic().disable();

            http.authorizeHttpRequests().anyRequest().authenticated();

            http.exceptionHandling().authenticationEntryPoint(unauthorizedEntryPoint);

            return http.build();
        }
    }
}
