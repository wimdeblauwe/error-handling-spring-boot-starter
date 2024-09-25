package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingProperties;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet.ServletErrorHandlingConfiguration;
import jakarta.validation.*;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.lang.annotation.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest
@ContextConfiguration(classes = {ServletErrorHandlingConfiguration.class,
        HandlerMethodValidationExceptionHandlerTest.TestController.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class HandlerMethodValidationExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void testHandlerMethodViolationException() throws Exception {
        mockMvc.perform(multipart("/test/update-event")
                                .part(new MockPart("eventRequest", null, "{}".getBytes(StandardCharsets.UTF_8), MediaType.APPLICATION_JSON))
                                .part(new MockPart("file", "file.jpg", new byte[0], MediaType.IMAGE_JPEG))
                                .with(request -> {
                                    request.setMethod(HttpMethod.PUT.name());
                                    return request;
                                })
                                .with(csrf()))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("message").value("There was a validation failure."))
               .andExpect(jsonPath("fieldErrors", hasSize(1)))
               .andExpect(jsonPath("fieldErrors..code", allOf(hasItem("REQUIRED_NOT_NULL"))))
               .andExpect(jsonPath("fieldErrors..property", allOf(hasItem("dateTime"))))
               .andExpect(jsonPath("fieldErrors..message", allOf(hasItem("must not be null"))))
               .andExpect(jsonPath("fieldErrors..rejectedValue", allOf(hasItem(nullValue()))))
               .andExpect(jsonPath("globalErrors", hasSize(1)))
               .andExpect(jsonPath("globalErrors..code", allOf(hasItem("ValidFileType"))))
               .andExpect(jsonPath("globalErrors..message", allOf(hasItem(""))))
        ;
    }

    @Test
    @WithMockUser
    void testHandlerMethodViolationException_customValidationAnnotationOverride(@Autowired ErrorHandlingProperties properties) throws Exception {
        properties.getCodes().put("ValidFileType", "INVALID_FILE_TYPE");
        properties.getMessages().put("ValidFileType", "The file type is invalid. Only text/plain and application/pdf allowed.");
        mockMvc.perform(multipart("/test/update-event")
                                .part(new MockPart("eventRequest", null, "{}".getBytes(StandardCharsets.UTF_8), MediaType.APPLICATION_JSON))
                                .part(new MockPart("file", "file.jpg", new byte[0], MediaType.IMAGE_JPEG))
                                .with(request -> {
                                    request.setMethod(HttpMethod.PUT.name());
                                    return request;
                                })
                                .with(csrf()))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("message").value("There was a validation failure."))
               .andExpect(jsonPath("fieldErrors", hasSize(1)))
               .andExpect(jsonPath("fieldErrors..code", allOf(hasItem("REQUIRED_NOT_NULL"))))
               .andExpect(jsonPath("fieldErrors..property", allOf(hasItem("dateTime"))))
               .andExpect(jsonPath("fieldErrors..message", allOf(hasItem("must not be null"))))
               .andExpect(jsonPath("fieldErrors..rejectedValue", allOf(hasItem(nullValue()))))
               .andExpect(jsonPath("globalErrors", hasSize(1)))
               .andExpect(jsonPath("globalErrors..code", allOf(hasItem("INVALID_FILE_TYPE"))))
               .andExpect(jsonPath("globalErrors..message", allOf(hasItem("The file type is invalid. Only text/plain and application/pdf allowed."))))
        ;
    }

    @RestController
    @RequestMapping
    static class TestController {

        @PutMapping("/test/update-event")
        public void updateEvent(
                @Valid @RequestPart EventRequest eventRequest,
                @Valid @ValidFileType @RequestPart MultipartFile file) {

        }
    }

    static class EventRequest {
        @NotNull
        private LocalDateTime dateTime;

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }
    }

    @Documented
    @Constraint(validatedBy = MultiPartFileValidator.class)
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @interface ValidFileType {

        // Default list of allowed file types
        String[] value() default {
                MediaType.TEXT_PLAIN_VALUE,
                MediaType.APPLICATION_PDF_VALUE
        };

        String message() default "";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    static class MultiPartFileValidator implements ConstraintValidator<ValidFileType, MultipartFile> {

        private List<String> allowed;

        @Override
        public void initialize(ValidFileType constraintAnnotation) {
            allowed = List.of(constraintAnnotation.value());
        }

        @Override
        public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
            return file == null || allowed.contains(file.getContentType());
        }
    }
}
