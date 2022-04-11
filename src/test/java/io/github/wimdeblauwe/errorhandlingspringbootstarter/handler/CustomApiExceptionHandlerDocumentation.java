package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet.ServletErrorHandlingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {ServletErrorHandlingConfiguration.class,
        CustomApiExceptionHandlerDocumentation.TestController.class})
@Import(CustomExceptionApiExceptionHandler.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CustomApiExceptionHandlerDocumentation {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void testConstraintViolationException() throws Exception {
        mockMvc.perform(post("/test")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf()))
               .andExpect(status().isInternalServerError())
               .andDo(print())
        ;
    }


    @RestController
    @RequestMapping("/test")
    public static class TestController {
        @PostMapping
        public void doPost() {
            throw new CustomException("parent exception message", new IOException("child IOException message"));
        }

    }

}
