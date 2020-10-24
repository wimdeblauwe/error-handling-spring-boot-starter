package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;


class ApiErrorResponseSerializationTest {

    @Test
    void testSerialization() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message"));
        assertThatJson(json).and(
                jsonAssert -> jsonAssert.node("code").isEqualTo("TEST_CODE"),
                jsonAssert -> jsonAssert.node("message").isEqualTo("Test message"),
                jsonAssert -> jsonAssert.node("httpStatus").isAbsent()

        );
    }

    @Test
    void testSerializationWithFieldError() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message");
        response.addFieldError(new ApiFieldError("FIELD_ERROR_CODE", "testField", "Test Field Message", "bad"));
        String json = objectMapper.writeValueAsString(response);
        assertThatJson(json).and(
                jsonAssert -> jsonAssert.node("code").isEqualTo("TEST_CODE"),
                jsonAssert -> jsonAssert.node("message").isEqualTo("Test message"),
                jsonAssert -> jsonAssert.node("httpStatus").isAbsent(),
                jsonAssert -> jsonAssert.node("fieldErrors[0].code").isEqualTo("FIELD_ERROR_CODE"),
                jsonAssert -> jsonAssert.node("fieldErrors[0].property").isEqualTo("testField"),
                jsonAssert -> jsonAssert.node("fieldErrors[0].message").isEqualTo("Test Field Message"),
                jsonAssert -> jsonAssert.node("fieldErrors[0].rejectedValue").isEqualTo("bad")
        );
    }

    @Test
    void testSerializationWithFieldErrorWithNullRejectedValue() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message");
        response.addFieldError(new ApiFieldError("FIELD_ERROR_CODE", "testField", "Test Field Message", null));
        String json = objectMapper.writeValueAsString(response);
        assertThatJson(json).and(
                jsonAssert -> jsonAssert.node("code").isEqualTo("TEST_CODE"),
                jsonAssert -> jsonAssert.node("message").isEqualTo("Test message"),
                jsonAssert -> jsonAssert.node("httpStatus").isAbsent(),
                jsonAssert -> jsonAssert.node("fieldErrors[0].code").isEqualTo("FIELD_ERROR_CODE"),
                jsonAssert -> jsonAssert.node("fieldErrors[0].property").isEqualTo("testField"),
                jsonAssert -> jsonAssert.node("fieldErrors[0].message").isEqualTo("Test Field Message"),
                jsonAssert -> jsonAssert.node("fieldErrors[0].rejectedValue").isNull()
        );
    }

    @Test
    void testSerializationWithGlobalError() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
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
        ObjectMapper objectMapper = new ObjectMapper();
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
        ObjectMapper objectMapper = new ObjectMapper();
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
}
