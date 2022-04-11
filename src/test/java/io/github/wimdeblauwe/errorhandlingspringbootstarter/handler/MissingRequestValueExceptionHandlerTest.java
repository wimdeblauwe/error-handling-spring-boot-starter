package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet.ServletErrorHandlingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest
@ContextConfiguration(classes = {ServletErrorHandlingConfiguration.class,
        MissingRequestValueExceptionHandlerTest.TestController.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MissingRequestValueExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void testMatrixVariable() throws Exception {
        mockMvc.perform(get("/matrix-variable"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("variableName").value("contactNumber"));
    }

    @Test
    @WithMockUser
    void testPathVariable() throws Exception {
        mockMvc.perform(get("/path-variable"))
               .andExpect(status().isInternalServerError())
               .andExpect(jsonPath("code").value("MISSING_PATH_VARIABLE"))
               .andExpect(jsonPath("parameterName").value("id"))
               .andExpect(jsonPath("parameterType").value("String"));
    }

    @Test
    @WithMockUser
    void testRequestCookie() throws Exception {
        mockMvc.perform(get("/request-cookie"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("cookieName").value("favorite"))
               .andExpect(jsonPath("parameterName").value("favoriteCookie"))
               .andExpect(jsonPath("parameterType").value("String"));
    }

    @Test
    @WithMockUser
    void testRequestHeader() throws Exception {
        mockMvc.perform(get("/request-header"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("headerName").value("X-Custom-Header"))
               .andExpect(jsonPath("parameterName").value("customHeader"))
               .andExpect(jsonPath("parameterType").value("String"));
    }

    @Test
    @WithMockUser
    void testMissingServletRequestParameter() throws Exception {
        mockMvc.perform(get("/missing-servlet-request-parameter"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("code").value("VALIDATION_FAILED"))
               .andExpect(jsonPath("parameterName").value("test"))
               .andExpect(jsonPath("parameterType").value("String"));
    }

    @RestController
    @RequestMapping
    public static class TestController {

        @GetMapping("/matrix-variable")
        public void matrixVariable(@MatrixVariable String contactNumber) {
        }

        @GetMapping("/path-variable")
        public void pathVariable(@PathVariable("id") String id) {
        }

        @GetMapping("/request-cookie")
        public void requestCookie(@CookieValue("favorite") String favoriteCookie) {
        }

        @GetMapping("/request-header")
        public void requestHeader(@RequestHeader("X-Custom-Header") String customHeader) {
        }

        @GetMapping("/missing-servlet-request-parameter")
        public ResponseEntity<Integer> missingServletRequestParameter(@RequestParam @NotEmpty String test) {
            return ResponseEntity.ok(1);
        }

    }
}
