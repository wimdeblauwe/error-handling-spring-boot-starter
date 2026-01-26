package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;


import io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet.ServletErrorHandlingConfiguration;
import jakarta.validation.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes = {ServletErrorHandlingConfiguration.class,
        ConstraintViolationApiExceptionHandlerWithProblemDetailTest.TestController.class,
        ConstraintViolationApiExceptionHandlerWithProblemDetailTest.TestParameterValidationController.class})
@Import(ConstraintViolationApiExceptionHandlerWithProblemDetailTest.TestService.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = "error.handling.use-problem-detail-format=true")
class ConstraintViolationApiExceptionHandlerWithProblemDetailTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void testConstraintViolationExceptionUsingProblemDetailFormat() throws Exception {
        mockMvc.perform(post("/test/validation")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"value2\": \"\"}")
                                .with(csrf()))
               .andExpect(status().isBadRequest())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("title").value("Bad Request"))
               .andExpect(jsonPath("type").value("validation-failed"))
               .andExpect(jsonPath("detail").value("Validation failed. Error count: 3"))
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


    @RestController
    @RequestMapping
    public static class TestController {
        @Autowired
        private TestService service;

        @PostMapping("/test/validation")
        public void doPostWithoutValidation(@RequestBody TestRequestBody requestBody) {
            service.doSomething(requestBody);
        }

        @PostMapping("/test/person-validation")
        public void doPersonPostWithoutValidation(@RequestBody Person requestBody) {
            service.doSomethingWithPerson(requestBody);
        }

        @PostMapping("/test/list-validation")
        public void doListPostWithoutValidation(@RequestBody List<Person> requestBody) {
            service.doSomethingWithList(requestBody);
        }

        @PostMapping("/test/set-validation")
        public void doSetPostWithoutValidation(@RequestBody Set<Person> requestBody) {
            service.doSomethingWithSet(requestBody);
        }

        @PostMapping("/test/map-validation")
        public void doMapPostWithoutValidation(@RequestBody Map<String, Person> requestBody) {
            service.doSomethingWithMap(requestBody);
        }

        @PostMapping("/test/multi-nested-validation")
        public void doMultiNestedValidation(@RequestBody MultiNestedRequest request) {
            service.doSomethingWithMultiNested(request);
        }
    }

    @RestController
    @RequestMapping
    @Validated
    public static class TestParameterValidationController {

        @GetMapping("/test/validation/parameter-validation")
        public void someGetMethod(@RequestParam("page") @Min(value = 0) int page) {

        }
    }

    @Service
    @Validated
    public static class TestService {
        void doSomething(@Valid TestRequestBody requestBody) {

        }

        void doSomethingWithPerson(@Valid Person requestBody) {

        }

        void doSomethingWithList(List<@Valid Person> requestBody) {

        }

        void doSomethingWithSet(Set<@Valid Person> requestBody) {

        }

        void doSomethingWithMap(Map<String, @Valid Person> requestBody) {

        }

        public void doSomethingWithMultiNested(@Valid MultiNestedRequest request) {

        }
    }

    public static class Person {

        @Size(min = 1, max = 255)
        private String name;

        private List<@Valid Person> kids = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Person> getKids() {
            return kids;
        }

        public void setKids(List<Person> kids) {
            this.kids = kids;
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

    public static class MultiNestedRequest {
        @Valid
        private MultiNestedLevel1 level1;

        public MultiNestedLevel1 getLevel1() {
            return level1;
        }

        public void setLevel1(MultiNestedLevel1 level1) {
            this.level1 = level1;
        }
    }

    public static class MultiNestedLevel1 {
        @Valid
        private MultiNestedLevel2 level2;

        public MultiNestedLevel2 getLevel2() {
            return level2;
        }

        public void setLevel2(MultiNestedLevel2 level2) {
            this.level2 = level2;
        }
    }

    public static class MultiNestedLevel2 {
        @NotBlank
        private String fieldAtLevel2;

        public String getFieldAtLevel2() {
            return fieldAtLevel2;
        }

        public void setFieldAtLevel2(String fieldAtLevel2) {
            this.fieldAtLevel2 = fieldAtLevel2;
        }
    }
}
