package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

@JsonTest
@Import(ErrorHandlingProperties.class)
class ApiErrorResponseSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ErrorHandlingProperties properties;

    @Test
    void testSerialization() throws IOException {
        String json = objectMapper.writeValueAsString(new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message"));
        assertThatJson(json).and(
                jsonAssert -> jsonAssert.node("code").isEqualTo("TEST_CODE"),
                jsonAssert -> jsonAssert.node("message").isEqualTo("Test message"),
                jsonAssert -> jsonAssert.node("httpStatus").isAbsent()

        );
    }

    @Test
    void testSerializationWithFieldError() throws IOException {
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message");
        response.addFieldError(new ApiFieldError("FIELD_ERROR_CODE", "testField", "Test Field Message", "bad", "path"));
        String json = objectMapper.writeValueAsString(response);
        assertThatJson(json).and(
                jsonAssert -> jsonAssert.node("code").isEqualTo("TEST_CODE"),
                jsonAssert -> jsonAssert.node("message").isEqualTo("Test message"),
                jsonAssert -> jsonAssert.node("httpStatus").isAbsent(),
                jsonAssert -> jsonAssert.node("fieldErrors[0].code").isEqualTo("FIELD_ERROR_CODE"),
                jsonAssert -> jsonAssert.node("fieldErrors[0].property").isEqualTo("testField"),
                jsonAssert -> jsonAssert.node("fieldErrors[0].message").isEqualTo("Test Field Message"),
                jsonAssert -> jsonAssert.node("fieldErrors[0].rejectedValue").isEqualTo("bad"),
                jsonAssert -> jsonAssert.node("fieldErrors[0].path").isEqualTo("path")
        );
    }

    @Test
    void testSerializationWithFieldErrorWithNullRejectedValue() throws IOException {
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message");
        response.addFieldError(new ApiFieldError("FIELD_ERROR_CODE", "testField", "Test Field Message", null, "path"));
        String json = objectMapper.writeValueAsString(response);
        assertThatJson(json).and(
                jsonAssert -> jsonAssert.node("code").isEqualTo("TEST_CODE"),
                jsonAssert -> jsonAssert.node("message").isEqualTo("Test message"),
                jsonAssert -> jsonAssert.node("httpStatus").isAbsent(),
                jsonAssert -> jsonAssert.node("fieldErrors[0].code").isEqualTo("FIELD_ERROR_CODE"),
                jsonAssert -> jsonAssert.node("fieldErrors[0].property").isEqualTo("testField"),
                jsonAssert -> jsonAssert.node("fieldErrors[0].message").isEqualTo("Test Field Message"),
                jsonAssert -> jsonAssert.node("fieldErrors[0].rejectedValue").isNull(),
                jsonAssert -> jsonAssert.node("fieldErrors[0].path").isEqualTo("path")
        );
    }

    @Test
    void testSerializationWithGlobalError() throws IOException {
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message");
        response.addGlobalError(new ApiGlobalError("GLOBAL_ERROR_CODE", "Test Global Message"));
        String json = objectMapper.writeValueAsString(response);
        assertThatJson(json).and(
                jsonAssert -> jsonAssert.node("code").isEqualTo("TEST_CODE"),
                jsonAssert -> jsonAssert.node("message").isEqualTo("Test message"),
                jsonAssert -> jsonAssert.node("httpStatus").isAbsent(),
                jsonAssert -> jsonAssert.node("globalErrors[0].code").isEqualTo("GLOBAL_ERROR_CODE"),
                jsonAssert -> jsonAssert.node("globalErrors[0].message").isEqualTo("Test Global Message")
        );
    }

    @Test
    void testSerializationWithErrorProperty() throws IOException {
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message");
        response.addErrorProperty("property1", "stringValue");
        response.addErrorProperty("property2", 15);
        String json = objectMapper.writeValueAsString(response);
        assertThatJson(json).and(
                jsonAssert -> jsonAssert.node("code").isEqualTo("TEST_CODE"),
                jsonAssert -> jsonAssert.node("message").isEqualTo("Test message"),
                jsonAssert -> jsonAssert.node("httpStatus").isAbsent(),
                jsonAssert -> jsonAssert.node("property1").isEqualTo("stringValue"),
                jsonAssert -> jsonAssert.node("property2").isEqualTo(15)
        );
    }

    @Test
    void testSerializationWithErrorPropertyThatIsNull() throws IOException {
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message");
        response.addErrorProperty("property1", null);
        String json = objectMapper.writeValueAsString(response);
        assertThatJson(json).and(
                jsonAssert -> jsonAssert.node("code").isEqualTo("TEST_CODE"),
                jsonAssert -> jsonAssert.node("message").isEqualTo("Test message"),
                jsonAssert -> jsonAssert.node("httpStatus").isAbsent(),
                jsonAssert -> jsonAssert.node("property1").isNull()
        );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testHttpStatusInJsonResponse() throws IOException {
        properties.setHttpStatusInJsonResponse(true);
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_REQUEST, "TEST_CODE", "Test message");

        String json = objectMapper.writeValueAsString(response);
        assertThatJson(json).and(
                jsonAssert -> jsonAssert.node("status").isEqualTo(400)
        );
    }

    @Test
    void testHttpStatusInJsonResponseDisabledByDefault() throws IOException {
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_REQUEST, "TEST_CODE", "Test message");

        String json = objectMapper.writeValueAsString(response);
        assertThatJson(json).and(
                jsonAssert -> jsonAssert.node("status").isAbsent()
        );
    }

    @Nested
    class CustomFieldNamesTests {
        @BeforeEach
        void setCustomName() {
            properties.getJsonFieldNames().setMessage("description");
            properties.getJsonFieldNames().setCode("errorCode");
            properties.getJsonFieldNames().setFieldErrors("fieldFailures");
            properties.getJsonFieldNames().setGlobalErrors("globalFailures");
        }

        @AfterEach
        void resetCustomName() {
            properties.getJsonFieldNames().setMessage("message");
            properties.getJsonFieldNames().setCode("code");
            properties.getJsonFieldNames().setFieldErrors("fieldErrors");
            properties.getJsonFieldNames().setGlobalErrors("globalErrors");
        }

        @Test
        void testSerializationWithCustomMessageFieldName() throws IOException {
            String json = objectMapper.writeValueAsString(new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message"));
            assertThatJson(json).and(
                    jsonAssert -> jsonAssert.node("errorCode").isEqualTo("TEST_CODE"),
                    jsonAssert -> jsonAssert.node("description").isEqualTo("Test message"),
                    jsonAssert -> jsonAssert.node("httpStatus").isAbsent()

            );
        }

        @Test
        void testSerializationWithFieldError() throws IOException {
            ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message");
            response.addFieldError(new ApiFieldError("FIELD_ERROR_CODE", "testField", "Test Field Message", "bad", "path"));
            String json = objectMapper.writeValueAsString(response);
            assertThatJson(json).and(
                    jsonAssert -> jsonAssert.node("errorCode").isEqualTo("TEST_CODE"),
                    jsonAssert -> jsonAssert.node("description").isEqualTo("Test message"),
                    jsonAssert -> jsonAssert.node("httpStatus").isAbsent(),
                    jsonAssert -> jsonAssert.node("fieldFailures[0].errorCode").isEqualTo("FIELD_ERROR_CODE"),
                    jsonAssert -> jsonAssert.node("fieldFailures[0].property").isEqualTo("testField"),
                    jsonAssert -> jsonAssert.node("fieldFailures[0].description").isEqualTo("Test Field Message"),
                    jsonAssert -> jsonAssert.node("fieldFailures[0].rejectedValue").isEqualTo("bad"),
                    jsonAssert -> jsonAssert.node("fieldFailures[0].path").isEqualTo("path")
            );
        }

        @Test
        void testSerializationWithGlobalError() throws IOException {
            ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message");
            response.addGlobalError(new ApiGlobalError("GLOBAL_ERROR_CODE", "Test Global Message"));
            String json = objectMapper.writeValueAsString(response);
            assertThatJson(json).and(
                    jsonAssert -> jsonAssert.node("errorCode").isEqualTo("TEST_CODE"),
                    jsonAssert -> jsonAssert.node("description").isEqualTo("Test message"),
                    jsonAssert -> jsonAssert.node("httpStatus").isAbsent(),
                    jsonAssert -> jsonAssert.node("globalFailures[0].errorCode").isEqualTo("GLOBAL_ERROR_CODE"),
                    jsonAssert -> jsonAssert.node("globalFailures[0].description").isEqualTo("Test Global Message")
            );
        }

    }
}
