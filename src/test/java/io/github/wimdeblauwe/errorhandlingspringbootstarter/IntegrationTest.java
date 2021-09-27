package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = IntegrationTestRestController.class,
        properties = {"spring.main.allow-bean-definition-overriding=true",
                "error.handling.full-stacktrace-http-statuses[0]=500"})
@Import(IntegrationTest.WebSecurityConfig.class)
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
               .andExpect(status().isBadRequest());
    }

    static class WebSecurityConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests(registry -> registry.anyRequest().permitAll());
        }

        @Bean
        public AuthenticationManager authenticationManagerBean() throws Exception {
            // Although this seems like useless code,
            // it is required to prevent Spring Boot creating a default password
            return super.authenticationManagerBean();
        }
    }
}
