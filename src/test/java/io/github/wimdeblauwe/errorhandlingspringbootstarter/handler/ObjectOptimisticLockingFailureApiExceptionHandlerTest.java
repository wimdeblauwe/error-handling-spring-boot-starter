package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;


import io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet.ServletErrorHandlingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.test.context.support.WithMockUser;
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
        ObjectOptimisticLockingFailureApiExceptionHandlerTest.TestController.class})
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

    @RestController
    @RequestMapping("/test/object-optimistic-locking-failure")
    public static class TestController {

        @GetMapping
        public void throwObjectOptimisticLockingFailureException() {
            throw new ObjectOptimisticLockingFailureException("com.example.user.User", 1L);
        }
    }
}
