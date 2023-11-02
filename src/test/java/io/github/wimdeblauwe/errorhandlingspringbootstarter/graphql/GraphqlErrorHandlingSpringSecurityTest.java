package io.github.wimdeblauwe.errorhandlingspringbootstarter.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.UnauthorizedEntryPoint;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet.ServletErrorHandlingConfiguration;
import jdk.jfr.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@EnableAutoConfiguration
@ContextConfiguration(classes = {ServletErrorHandlingConfiguration.class,
        GraphqlErrorHandlingSpringSecurityTest.GraphqlTestsConfiguration.class})
@Import({PersonController.class})
@AutoConfigureHttpGraphQlTester
class GraphqlErrorHandlingSpringSecurityTest {
    /**
     * Usually for security scenarios, the requests will be blocked by Spring security before they even get processed by the Controllers. This is because the 401 occurs at transport level.
     * See <a href="https://github.com/spring-projects/spring-graphql/issues/778">Github issue</a>.
     * In this case, we will get the default Server error handling with expected responses.
     */

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HttpGraphQlTester graphQlTester;


    @Test
    void testUnauthorized() throws Exception {
        String query = "{ findPersonById(id: 1) { id firstName lastName } }";
        mockMvc.perform(post("/graphql").content(query.getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Type", "application/json;charset=UTF-8"))
                .andExpect(jsonPath("code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("message").value("Full authentication is required to access this resource"));
    }

    @Test
    @WithMockUser
    void testAuthorized() throws Exception {
        String query = "{ \"query\": \"{ findPersonById(id: 1) { id firstName lastName } } \"}";
        mockMvc.perform(post("/graphql").content(query.getBytes(StandardCharsets.UTF_8))
                                        .accept(MediaType.APPLICATION_JSON_VALUE)
                                        .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andDo(print());
    }

    @TestConfiguration
    @Import({JacksonAutoConfiguration.class})
    static class GraphqlTestsConfiguration {

        @Bean
        public UnauthorizedEntryPoint unauthorizedEntryPoint(HttpStatusMapper httpStatusMapper,
                                                             ErrorCodeMapper errorCodeMapper,
                                                             ErrorMessageMapper errorMessageMapper,
                                                             ObjectMapper objectMapper) {
            return new UnauthorizedEntryPoint(httpStatusMapper, errorCodeMapper, errorMessageMapper, objectMapper);
        }

        @Bean
        public UserDetailsService userDetailsService() {
            UserDetails user = User.builder()
                                   .username("user")
                                   .password(passwordEncoder().encode("password"))
                                   .roles("USER")
                                   .build();
            return new InMemoryUserDetailsManager(user);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                       UnauthorizedEntryPoint unauthorizedEntryPoint) throws Exception {
            http.csrf().disable();
            http.httpBasic(Customizer.withDefaults());
            http.authorizeHttpRequests().anyRequest().authenticated();
            http.exceptionHandling().authenticationEntryPoint(unauthorizedEntryPoint);
            return http.build();
        }
    }
}
