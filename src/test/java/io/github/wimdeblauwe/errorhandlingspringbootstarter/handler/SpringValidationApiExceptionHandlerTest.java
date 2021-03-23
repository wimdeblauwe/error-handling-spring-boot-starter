package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;


import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingConfiguration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {ErrorHandlingConfiguration.class,
        SpringValidationApiExceptionHandlerTest.TestController.class})
@Import(SpringValidationApiExceptionHandlerTest.TestService.class)
class SpringValidationApiExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void testMethodArgumentNotValidException() throws Exception {
        mockMvc.perform(post("/test/validation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"value2\": \"\"}")
                                .with(csrf()))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("message").value("Validation failed for object='testRequestBody'. Error count: 2"))
               .andExpect(jsonPath("fieldErrors", hasSize(2)))
               .andExpect(jsonPath("fieldErrors..code", allOf(hasItem("REQUIRED_NOT_NULL"), hasItem("INVALID_SIZE"))))
               .andExpect(jsonPath("fieldErrors..property", allOf(hasItem("value"), hasItem("value2"))))
               .andExpect(jsonPath("fieldErrors..message", allOf(hasItem("must not be null"), hasItem("size must be between 1 and 255"))))
               .andExpect(jsonPath("fieldErrors..rejectedValue", allOf(hasItem(Matchers.nullValue()), hasItem(""))))
        ;
    }

    @Test
    @WithMockUser
    void testHttpMessageNotReadableException() throws Exception {
        mockMvc.perform(post("/test/validation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalidjsonhere}")
                                .with(csrf()))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("MESSAGE_NOT_READABLE"))
               .andExpect(jsonPath("message", Matchers.startsWith("JSON parse error: Unexpected character ('i' (code 105))")))
        ;
    }

    @Test
    @WithMockUser
    void testConstraintViolationException() throws Exception {
        mockMvc.perform(post("/test/validation/no")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"value2\": \"\"}")
                                .with(csrf()))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("message").value("Validation failed. Error count: 2"))
               .andExpect(jsonPath("fieldErrors", hasSize(2)))
               .andExpect(jsonPath("fieldErrors..code", allOf(hasItem("REQUIRED_NOT_NULL"), hasItem("INVALID_SIZE"))))
               .andExpect(jsonPath("fieldErrors..property", allOf(hasItem("doSomething.requestBody.value"), hasItem("doSomething.requestBody.value2"))))
               .andExpect(jsonPath("fieldErrors..message", allOf(hasItem("must not be null"), hasItem("size must be between 1 and 255"))))
               .andExpect(jsonPath("fieldErrors..rejectedValue", allOf(hasItem(Matchers.nullValue()), hasItem(""))))
        ;
    }


    @RestController
    @RequestMapping("/test/validation")
    public static class TestController {
        @Autowired
        private TestService service;

        @PostMapping
        public void doPost(@Valid @RequestBody TestRequestBody requestBody) {

        }

        @PostMapping("/no")
        public void doPostWithoutValidation(@RequestBody TestRequestBody requestBody) {
            service.doSomething(requestBody);
        }
    }

    @Service
    @Validated
    public static class TestService {
        void doSomething(@Valid TestRequestBody requestBody) {

        }
    }

    public static class TestRequestBody {
        @NotNull
        private String value;

        @NotNull
        @Size(min = 1, max = 255)
        private String value2;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue2() {
            return value2;
        }

        public void setValue2(String value2) {
            this.value2 = value2;
        }
    }
}
