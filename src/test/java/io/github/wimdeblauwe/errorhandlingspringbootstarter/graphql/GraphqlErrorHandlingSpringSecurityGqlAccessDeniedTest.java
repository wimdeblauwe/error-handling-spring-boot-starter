package io.github.wimdeblauwe.errorhandlingspringbootstarter.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.UnauthorizedEntryPoint;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet.ServletErrorHandlingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EnableAutoConfiguration
@ContextConfiguration(classes = {ServletErrorHandlingConfiguration.class,
        GraphqlErrorHandlingSpringSecurityGqlAccessDeniedTest.GraphqlTestsConfiguration.class})
@Import({PersonController.class})
@AutoConfigureHttpGraphQlTester
class GraphqlErrorHandlingSpringSecurityGqlAccessDeniedTest {
    /**
     * Usually for security scenarios, the requests will be blocked by Spring security before they even get processed by the Controllers. This is because the 401 occurs at transport level.
     * See <a href="https://github.com/spring-projects/spring-graphql/issues/778">Github issue</a>.
     * In this case, we will get the default Server error handling with expected responses.
     */

    @Autowired
    private HttpGraphQlTester graphQlTester;

    @Test
    void testAccessDenied() {
        String query = "{ findPersonThrowAccessDeniedException { id firstName lastName } }";
        graphQlTester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getErrorType().toString().equalsIgnoreCase("FORBIDDEN"))
                .expect(responseError -> responseError.getMessage().equalsIgnoreCase("Graphql Access Denied"))
                .expect(responseError -> responseError.getExtensions().get("code").equals("ACCESS_DENIED"));
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
        public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                       UnauthorizedEntryPoint unauthorizedEntryPoint) throws Exception {
            http.csrf().disable();
            http.httpBasic(Customizer.withDefaults());
            // could not break through security with basic auth...
            http.authorizeHttpRequests().anyRequest().permitAll();
            http.exceptionHandling().authenticationEntryPoint(unauthorizedEntryPoint);
            return http.build();
        }
    }
}
