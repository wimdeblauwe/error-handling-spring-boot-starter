package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;


import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes = {
        ObjectOptimisticLockingFailureApiExceptionHandlerTest.TestController.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ObjectOptimisticLockingFailureApiExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void testOOLF() throws Exception {
        mockMvc.perform(get("/test/object-optimistic-locking-failure"))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("code").value("OPTIMISTIC_LOCKING_ERROR"))
               .andExpect(jsonPath("message").value("Object of class [com.example.user.User] with identifier [1]: optimistic locking failed"))
               .andExpect(jsonPath("persistentClassName").value("com.example.user.User"))
               .andExpect(jsonPath("identifier").value("1"))
        ;
    }

    @Test
    @WithMockUser
    void testOOLFWithProblemDetailFormat(@Autowired ErrorHandlingProperties properties) throws Exception {
        properties.setUseProblemDetailFormat(true);
        mockMvc.perform(get("/test/object-optimistic-locking-failure"))
               .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("title").value("Conflict"))
               .andExpect(jsonPath("type").value("optimistic-locking-error"))
               .andExpect(jsonPath("detail").value("Object of class [com.example.user.User] with identifier [1]: optimistic locking failed"))
               .andExpect(jsonPath("persistentClassName").value("com.example.user.User"))
               .andExpect(jsonPath("identifier").value("1"))
        ;
    }

    @Test
    @WithMockUser
    void testOOLFWithProblemDetailFormatAndTypePrefix(@Autowired ErrorHandlingProperties properties) throws Exception {
        properties.setUseProblemDetailFormat(true);
        properties.setProblemDetailTypePrefix("https://example.org/");
        mockMvc.perform(get("/test/object-optimistic-locking-failure"))
               .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("title").value("Conflict"))
               .andExpect(jsonPath("type").value("https://example.org/optimistic-locking-error"))
               .andExpect(jsonPath("detail").value("Object of class [com.example.user.User] with identifier [1]: optimistic locking failed"))
               .andExpect(jsonPath("persistentClassName").value("com.example.user.User"))
               .andExpect(jsonPath("identifier").value("1"))
        ;
    }

    @RestController
    @RequestMapping("/test/object-optimistic-locking-failure")
    public static class TestController {

        @GetMapping
        public void throwObjectOptimisticLockingFailureException() {
            throw new ObjectOptimisticLockingFailureException("com.example.user.User", 1L);
        }
    }
}
