package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet.ServletErrorHandlingConfiguration;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {ServletErrorHandlingConfiguration.class,
        BindApiExceptionHandlerTest.TestController.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BindApiExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void testFieldValidation() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/test/field-validation")
                                              .queryParam("param1", "foo"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("fieldErrors", hasSize(1)))
               .andExpect(jsonPath("fieldErrors[0].code").value("REQUIRED_NOT_NULL"))
               .andExpect(jsonPath("fieldErrors[0].message").value("must not be null"))
               .andExpect(jsonPath("fieldErrors[0].property").value("param2"))
               .andExpect(jsonPath("fieldErrors[0].rejectedValue").value(Matchers.nullValue()))
               .andExpect(jsonPath("fieldErrors[0].path").value("param2"))
        ;
    }

    @Test
    @WithMockUser
    void testFieldValidationWithCodeOverride(@Autowired ErrorHandlingProperties properties) throws Exception {
        properties.getCodes().put("NotNull", "SHOULD_NOT_BE_NULL");
        mockMvc.perform(MockMvcRequestBuilders.get("/test/field-validation")
                                              .queryParam("param1", "foo"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("fieldErrors", hasSize(1)))
               .andExpect(jsonPath("fieldErrors[0].code").value("SHOULD_NOT_BE_NULL"))
               .andExpect(jsonPath("fieldErrors[0].message").value("must not be null"))
               .andExpect(jsonPath("fieldErrors[0].property").value("param2"))
               .andExpect(jsonPath("fieldErrors[0].rejectedValue").value(Matchers.nullValue()))
               .andExpect(jsonPath("fieldErrors[0].path").value("param2"))
        ;
    }

    @Test
    @WithMockUser
    void testFieldValidationWithCodeOverrideOnFieldLevel(@Autowired ErrorHandlingProperties properties) throws Exception {
        properties.getCodes().put("param1.NotNull", "PARAM_1_SHOULD_NOT_BE_NULL");
        properties.getCodes().put("param2.NotNull", "PARAM_2_SHOULD_NOT_BE_NULL");
        mockMvc.perform(MockMvcRequestBuilders.get("/test/field-validation"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("fieldErrors", hasSize(2)))
               .andExpect(jsonPath("fieldErrors..code").value(Matchers.containsInAnyOrder("PARAM_1_SHOULD_NOT_BE_NULL", "PARAM_2_SHOULD_NOT_BE_NULL")))
               .andExpect(jsonPath("fieldErrors..property").value(Matchers.containsInAnyOrder("param1", "param2")));
    }

    @Test
    @WithMockUser
    void testObjectValidation() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/test/object-validation")
                                              .queryParam("param1", "foo")
                                              .queryParam("param2", "bar"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("globalErrors", hasSize(1)))
               .andExpect(jsonPath("globalErrors[0].code").value("ValidRequest"))
               .andExpect(jsonPath("globalErrors[0].message").value("Param1 and Param2 cannot be combined"));
    }

    @Test
    @WithMockUser
    void testObjectValidationWithCodeOverride(@Autowired ErrorHandlingProperties properties) throws Exception {
        properties.getCodes().put("ValidRequest", "PARAMS_CANNOT_BE_COMBINED");
        mockMvc.perform(MockMvcRequestBuilders.get("/test/object-validation")
                                              .queryParam("param1", "foo")
                                              .queryParam("param2", "bar"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("globalErrors", hasSize(1)))
               .andExpect(jsonPath("globalErrors[0].code").value("PARAMS_CANNOT_BE_COMBINED"))
               .andExpect(jsonPath("globalErrors[0].message").value("Param1 and Param2 cannot be combined"));
    }

    @Test
    @WithMockUser
    void testObjectValidationWithMessageOverride(@Autowired ErrorHandlingProperties properties) throws Exception {
        properties.getMessages().put("ValidRequest", "Param1 and Param2 cannot be used together");
        mockMvc.perform(MockMvcRequestBuilders.get("/test/object-validation")
                                              .queryParam("param1", "foo")
                                              .queryParam("param2", "bar"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("globalErrors", hasSize(1)))
               .andExpect(jsonPath("globalErrors[0].code").value("ValidRequest"))
               .andExpect(jsonPath("globalErrors[0].message").value("Param1 and Param2 cannot be used together"));
    }

    @Test
    @WithMockUser
    void testTopLevelCodeOverride(@Autowired ErrorHandlingProperties properties) throws Exception {
        properties.getCodes().put("org.springframework.web.bind.MethodArgumentNotValidException", "METHOD_ARG_NOT_VALID");
        mockMvc.perform(MockMvcRequestBuilders.get("/test/field-validation")
                                              .queryParam("param1", "foo"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("METHOD_ARG_NOT_VALID"))
               .andExpect(jsonPath("fieldErrors", hasSize(1)))
               .andExpect(jsonPath("fieldErrors[0].code").value("REQUIRED_NOT_NULL"))
               .andExpect(jsonPath("fieldErrors[0].message").value("must not be null"))
               .andExpect(jsonPath("fieldErrors[0].property").value("param2"))
               .andExpect(jsonPath("fieldErrors[0].rejectedValue").value(Matchers.nullValue()))
               .andExpect(jsonPath("fieldErrors[0].path").value("param2"))
        ;
    }

    @Test
    @WithMockUser
    void testDisableAddingPath(@Autowired ErrorHandlingProperties properties) throws Exception {
        properties.getCodes().put("org.springframework.web.bind.MethodArgumentNotValidException", "METHOD_ARG_NOT_VALID");
        properties.setAddPathToError(false);
        mockMvc.perform(MockMvcRequestBuilders.get("/test/field-validation")
                                              .queryParam("param1", "foo"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("METHOD_ARG_NOT_VALID"))
               .andExpect(jsonPath("fieldErrors", hasSize(1)))
               .andExpect(jsonPath("fieldErrors[0].code").value("REQUIRED_NOT_NULL"))
               .andExpect(jsonPath("fieldErrors[0].message").value("must not be null"))
               .andExpect(jsonPath("fieldErrors[0].property").value("param2"))
               .andExpect(jsonPath("fieldErrors[0].rejectedValue").value(Matchers.nullValue()))
               .andExpect(jsonPath("fieldErrors[0].path").doesNotExist())
        ;
    }

    @Test
    @WithMockUser
    void testRequestParam() throws Exception {
        // Note this is handled by ConstraintViolationApiExceptionHandler, but it seemed test wise to
        // more appropriate to include it here
        mockMvc.perform(MockMvcRequestBuilders.get("/test/request-param")
                                              .queryParam("param", ""))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("parameterErrors", hasSize(1)))
               .andExpect(jsonPath("parameterErrors[0].code").value("REQUIRED_NOT_BLANK"))
               .andExpect(jsonPath("parameterErrors[0].message").value("must not be blank"))
               .andExpect(jsonPath("parameterErrors[0].parameter").value("param"))
               .andExpect(jsonPath("parameterErrors[0].rejectedValue").value(""))
        ;
    }

    @RestController
    @RequestMapping
    @Validated
    public static class TestController {
        @GetMapping("/test/field-validation")
        public void someGetMethod(@Valid TestRequest testRequest) {

        }

        @GetMapping("/test/object-validation")
        public void someGetMethodWithObjectValidation(@Valid TestRequestForObjectValidation testRequest) {

        }

        @GetMapping("/test/request-param")
        public void someGetMethod(@NotBlank @RequestParam("param") String param) {

        }

    }

    public static class TestRequest {
        @NotNull
        private String param1;
        @NotNull
        private String param2;

        public String getParam1() {
            return param1;
        }

        public void setParam1(String param1) {
            this.param1 = param1;
        }

        public String getParam2() {
            return param2;
        }

        public void setParam2(String param2) {
            this.param2 = param2;
        }
    }

    @ValidRequest
    public static class TestRequestForObjectValidation {
        private String param1;
        private String param2;

        public String getParam1() {
            return param1;
        }

        public void setParam1(String param1) {
            this.param1 = param1;
        }

        public String getParam2() {
            return param2;
        }

        public void setParam2(String param2) {
            this.param2 = param2;
        }
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = RequestValidator.class)
    public @interface ValidRequest {
        String message() default "Request not valid";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};

    }

    public static class RequestValidator implements ConstraintValidator<ValidRequest, TestRequestForObjectValidation> {

        @Override
        public boolean isValid(TestRequestForObjectValidation request,
                               ConstraintValidatorContext context) {
            if (request.getParam1() != null && request.getParam2() != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Param1 and Param2 cannot be combined")
                       .addConstraintViolation();
                return false;
            }
            return true;
        }
    }
}
