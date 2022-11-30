package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;


import io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet.ServletErrorHandlingConfiguration;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {ServletErrorHandlingConfiguration.class,
        BindApiExceptionHandlerWithMethodArgumentNotValidTest.TestController.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BindApiExceptionHandlerWithMethodArgumentNotValidTest {

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
               .andExpect(jsonPath("message").value("Validation failed for object='testRequestBody'. Error count: 3"))
               .andExpect(jsonPath("fieldErrors", hasSize(2)))
               .andExpect(jsonPath("fieldErrors..code", allOf(hasItem("REQUIRED_NOT_NULL"), hasItem("INVALID_SIZE"))))
               .andExpect(jsonPath("fieldErrors..property", allOf(hasItem("value"), hasItem("value2"))))
               .andExpect(jsonPath("fieldErrors..message", allOf(hasItem("must not be null"), hasItem("size must be between 1 and 255"))))
               .andExpect(jsonPath("fieldErrors..rejectedValue", allOf(hasItem(Matchers.nullValue()), hasItem(""))))
               .andExpect(jsonPath("globalErrors", hasSize(1)))
               .andExpect(jsonPath("globalErrors..code", allOf(hasItem("ValuesEqual"))))
               .andExpect(jsonPath("globalErrors..message", allOf(hasItem("Values not equal"))))
        ;
    }

    @Test
    @WithMockUser
    void testErrorCodeOverride(@Autowired ErrorHandlingProperties properties) throws Exception {
        properties.getCodes().put("org.springframework.web.bind.MethodArgumentNotValidException", "ARG_NOT_VALID");
        mockMvc.perform(post("/test/validation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"value2\": \"\"}")
                                .with(csrf()))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("ARG_NOT_VALID"))
               .andExpect(jsonPath("message").value("Validation failed for object='testRequestBody'. Error count: 3"))
               .andExpect(jsonPath("fieldErrors", hasSize(2)))
               .andExpect(jsonPath("fieldErrors..code", allOf(hasItem("REQUIRED_NOT_NULL"), hasItem("INVALID_SIZE"))))
               .andExpect(jsonPath("fieldErrors..property", allOf(hasItem("value"), hasItem("value2"))))
               .andExpect(jsonPath("fieldErrors..message", allOf(hasItem("must not be null"), hasItem("size must be between 1 and 255"))))
               .andExpect(jsonPath("fieldErrors..rejectedValue", allOf(hasItem(Matchers.nullValue()), hasItem(""))))
               .andExpect(jsonPath("globalErrors", hasSize(1)))
               .andExpect(jsonPath("globalErrors..code", allOf(hasItem("ValuesEqual"))))
               .andExpect(jsonPath("globalErrors..message", allOf(hasItem("Values not equal"))))
        ;
    }

    @Test
    @WithMockUser
    void testFieldErrorCodeOverride(@Autowired ErrorHandlingProperties properties) throws Exception {
        properties.getCodes().put("NotNull", "NOT_NULL");
        mockMvc.perform(post("/test/validation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"value2\": \"\"}")
                                .with(csrf()))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("message").value("Validation failed for object='testRequestBody'. Error count: 3"))
               .andExpect(jsonPath("fieldErrors", hasSize(2)))
               .andExpect(jsonPath("fieldErrors..code", allOf(hasItem("NOT_NULL"), hasItem("INVALID_SIZE"))))
               .andExpect(jsonPath("fieldErrors..property", allOf(hasItem("value"), hasItem("value2"))))
               .andExpect(jsonPath("fieldErrors..message", allOf(hasItem("must not be null"), hasItem("size must be between 1 and 255"))))
               .andExpect(jsonPath("fieldErrors..rejectedValue", allOf(hasItem(Matchers.nullValue()), hasItem(""))))
               .andExpect(jsonPath("globalErrors", hasSize(1)))
               .andExpect(jsonPath("globalErrors..code", allOf(hasItem("ValuesEqual"))))
               .andExpect(jsonPath("globalErrors..message", allOf(hasItem("Values not equal"))))
        ;
    }

    @Test
    @WithMockUser
    void testFieldErrorCodeOverrideForSpecificField(@Autowired ErrorHandlingProperties properties) throws Exception {
        properties.getCodes().put("value.NotNull", "VALUE_NOT_NULL");
        mockMvc.perform(post("/test/validation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"value2\": \"\"}")
                                .with(csrf()))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("message").value("Validation failed for object='testRequestBody'. Error count: 3"))
               .andExpect(jsonPath("fieldErrors", hasSize(2)))
               .andExpect(jsonPath("fieldErrors..code", allOf(hasItem("VALUE_NOT_NULL"), hasItem("INVALID_SIZE"))))
               .andExpect(jsonPath("fieldErrors..property", allOf(hasItem("value"), hasItem("value2"))))
               .andExpect(jsonPath("fieldErrors..message", allOf(hasItem("must not be null"), hasItem("size must be between 1 and 255"))))
               .andExpect(jsonPath("fieldErrors..rejectedValue", allOf(hasItem(Matchers.nullValue()), hasItem(""))))
               .andExpect(jsonPath("globalErrors", hasSize(1)))
               .andExpect(jsonPath("globalErrors..code", allOf(hasItem("ValuesEqual"))))
               .andExpect(jsonPath("globalErrors..message", allOf(hasItem("Values not equal"))))
        ;
    }

    @Test
    @WithMockUser
    void testFieldErrorMessageOverride(@Autowired ErrorHandlingProperties properties) throws Exception {
        properties.getMessages().put("NotNull", "required not to be null");
        mockMvc.perform(post("/test/validation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"value2\": \"\"}")
                                .with(csrf()))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("message").value("Validation failed for object='testRequestBody'. Error count: 3"))
               .andExpect(jsonPath("fieldErrors", hasSize(2)))
               .andExpect(jsonPath("fieldErrors..code", allOf(hasItem("REQUIRED_NOT_NULL"), hasItem("INVALID_SIZE"))))
               .andExpect(jsonPath("fieldErrors..property", allOf(hasItem("value"), hasItem("value2"))))
               .andExpect(jsonPath("fieldErrors..message", allOf(hasItem("required not to be null"), hasItem("size must be between 1 and 255"))))
               .andExpect(jsonPath("fieldErrors..rejectedValue", allOf(hasItem(Matchers.nullValue()), hasItem(""))))
               .andExpect(jsonPath("globalErrors", hasSize(1)))
               .andExpect(jsonPath("globalErrors..code", allOf(hasItem("ValuesEqual"))))
               .andExpect(jsonPath("globalErrors..message", allOf(hasItem("Values not equal"))))
        ;
    }

    @Test
    @WithMockUser
    void testFieldErrorMessageOverrideForSpecificField(@Autowired ErrorHandlingProperties properties) throws Exception {
        properties.getMessages().put("value.NotNull", "value is required not to be null");
        mockMvc.perform(post("/test/validation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"value2\": \"\"}")
                                .with(csrf()))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("message").value("Validation failed for object='testRequestBody'. Error count: 3"))
               .andExpect(jsonPath("fieldErrors", hasSize(2)))
               .andExpect(jsonPath("fieldErrors..code", allOf(hasItem("REQUIRED_NOT_NULL"), hasItem("INVALID_SIZE"))))
               .andExpect(jsonPath("fieldErrors..property", allOf(hasItem("value"), hasItem("value2"))))
               .andExpect(jsonPath("fieldErrors..message", allOf(hasItem("value is required not to be null"), hasItem("size must be between 1 and 255"))))
               .andExpect(jsonPath("fieldErrors..rejectedValue", allOf(hasItem(Matchers.nullValue()), hasItem(""))))
               .andExpect(jsonPath("globalErrors", hasSize(1)))
               .andExpect(jsonPath("globalErrors..code", allOf(hasItem("ValuesEqual"))))
               .andExpect(jsonPath("globalErrors..message", allOf(hasItem("Values not equal"))))
        ;
    }

    @Test
    @WithMockUser
    void testGlobalErrorCodeOverride(@Autowired ErrorHandlingProperties properties) throws Exception {
        properties.getCodes().put("ValuesEqual", "VALUES_EQUAL");
        mockMvc.perform(post("/test/validation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"value2\": \"\"}")
                                .with(csrf()))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("message").value("Validation failed for object='testRequestBody'. Error count: 3"))
               .andExpect(jsonPath("fieldErrors", hasSize(2)))
               .andExpect(jsonPath("fieldErrors..code", allOf(hasItem("REQUIRED_NOT_NULL"), hasItem("INVALID_SIZE"))))
               .andExpect(jsonPath("fieldErrors..property", allOf(hasItem("value"), hasItem("value2"))))
               .andExpect(jsonPath("fieldErrors..message", allOf(hasItem("must not be null"), hasItem("size must be between 1 and 255"))))
               .andExpect(jsonPath("fieldErrors..rejectedValue", allOf(hasItem(Matchers.nullValue()), hasItem(""))))
               .andExpect(jsonPath("globalErrors", hasSize(1)))
               .andExpect(jsonPath("globalErrors..code", allOf(hasItem("VALUES_EQUAL"))))
               .andExpect(jsonPath("globalErrors..message", allOf(hasItem("Values not equal"))))
        ;
    }

    @Test
    @WithMockUser
    void testGlobalErrorMessageOverride(@Autowired ErrorHandlingProperties properties) throws Exception {
        properties.getMessages().put("ValuesEqual", "The values are unfortunately not equal.");
        mockMvc.perform(post("/test/validation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"value2\": \"\"}")
                                .with(csrf()))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("message").value("Validation failed for object='testRequestBody'. Error count: 3"))
               .andExpect(jsonPath("fieldErrors", hasSize(2)))
               .andExpect(jsonPath("fieldErrors..code", allOf(hasItem("REQUIRED_NOT_NULL"), hasItem("INVALID_SIZE"))))
               .andExpect(jsonPath("fieldErrors..property", allOf(hasItem("value"), hasItem("value2"))))
               .andExpect(jsonPath("fieldErrors..message", allOf(hasItem("must not be null"), hasItem("size must be between 1 and 255"))))
               .andExpect(jsonPath("fieldErrors..rejectedValue", allOf(hasItem(Matchers.nullValue()), hasItem(""))))
               .andExpect(jsonPath("globalErrors", hasSize(1)))
               .andExpect(jsonPath("globalErrors..code", allOf(hasItem("ValuesEqual"))))
               .andExpect(jsonPath("globalErrors..message", allOf(hasItem("The values are unfortunately not equal."))))
        ;
    }

    @RestController
    @RequestMapping("/test/validation")
    public static class TestController {
        @PostMapping
        public void doPost(@Valid @RequestBody TestRequestBody requestBody) {

        }

    }

    @ValuesEqual
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


    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = ValuesEqualValidator.class)
    public @interface ValuesEqual {
        String message() default "Values not equal";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};

    }

    public static class ValuesEqualValidator implements ConstraintValidator<ValuesEqual, TestRequestBody> {

        @Override
        public boolean isValid(TestRequestBody requestBody,
                               ConstraintValidatorContext context) {
            return Objects.equals(requestBody.getValue(), requestBody.getValue2());
        }
    }
}
