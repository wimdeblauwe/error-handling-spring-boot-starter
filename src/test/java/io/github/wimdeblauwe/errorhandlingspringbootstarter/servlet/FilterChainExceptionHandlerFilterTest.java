package io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.OncePerRequestFilter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {ServletErrorHandlingConfiguration.class,
        FilterChainExceptionHandlerFilterTest.TestController.class,
        FilterChainExceptionHandlerFilterTest.TestConfig.class})
@TestPropertySource(properties = "error.handling.handle-filter-chain-exceptions=true")
public class FilterChainExceptionHandlerFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void test() throws Exception {
        mockMvc.perform(get("/test/filter-chain"))
               .andExpect(status().is5xxServerError())
               .andExpect(jsonPath("code").value("RUNTIME"))
               .andExpect(jsonPath("message").value("Error in filter"));

    }

    @RestController
    @RequestMapping("/test/filter-chain")
    public static class TestController {

        @GetMapping
        public void doSomething() {
        }
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public FilterRegistrationBean<ThrowErrorFilter> filter() {
            FilterRegistrationBean<ThrowErrorFilter> registrationBean = new FilterRegistrationBean<>();
            registrationBean.setFilter(new ThrowErrorFilter());
            registrationBean.addUrlPatterns("/test/filter-chain");
            registrationBean.setOrder(2);

            return registrationBean;
        }
    }

    static class ThrowErrorFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
            throw new RuntimeException("Error in filter");
        }
    }
}
