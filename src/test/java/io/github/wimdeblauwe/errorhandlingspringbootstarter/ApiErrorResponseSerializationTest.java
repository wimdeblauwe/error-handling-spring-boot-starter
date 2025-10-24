package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
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

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@Import(ErrorHandlingProperties.class)
class ApiErrorResponseSerializationTest {

    @Autowired
    private JacksonTester<ApiErrorResponse> tester;
    @Autowired
    private ErrorHandlingProperties properties;

    @Test
    void testSerialization() throws IOException {
        JsonContent<ApiErrorResponse> content = tester.write(new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message"));
        assertThat(content).extractingJsonPathStringValue("$.code").isEqualTo("TEST_CODE");
        assertThat(content).extractingJsonPathStringValue("$.message").isEqualTo("Test message");
        assertThat(content).doesNotHaveJsonPath("$.httpStatus");
    }

    @Test
    void testSerializationWithFieldError() throws IOException {
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message");
        response.addFieldError(new ApiFieldError("FIELD_ERROR_CODE", "testField", "Test Field Message", "bad", "path"));
        JsonContent<ApiErrorResponse> content = tester.write(response);
        assertThat(content).extractingJsonPathStringValue("$.code").isEqualTo("TEST_CODE");
        assertThat(content).extractingJsonPathStringValue("$.message").isEqualTo("Test message");
        assertThat(content).doesNotHaveJsonPath("$.httpStatus");
        assertThat(content).extractingJsonPathStringValue("$.fieldErrors[0].code").isEqualTo("FIELD_ERROR_CODE");
        assertThat(content).extractingJsonPathStringValue("$.fieldErrors[0].property").isEqualTo("testField");
        assertThat(content).extractingJsonPathStringValue("$.fieldErrors[0].message").isEqualTo("Test Field Message");
        assertThat(content).extractingJsonPathStringValue("$.fieldErrors[0].rejectedValue").isEqualTo("bad");
        assertThat(content).extractingJsonPathStringValue("$.fieldErrors[0].path").isEqualTo("path");
    }

    @Test
    void testSerializationWithFieldErrorWithNullRejectedValue() throws IOException {
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message");
        response.addFieldError(new ApiFieldError("FIELD_ERROR_CODE", "testField", "Test Field Message", null, "path"));
        JsonContent<ApiErrorResponse> content = tester.write(response);
        assertThat(content).extractingJsonPathStringValue("$.code").isEqualTo("TEST_CODE");
        assertThat(content).extractingJsonPathStringValue("$.message").isEqualTo("Test message");
        assertThat(content).doesNotHaveJsonPath("$.httpStatus");
        assertThat(content).extractingJsonPathStringValue("$.fieldErrors[0].code").isEqualTo("FIELD_ERROR_CODE");
        assertThat(content).extractingJsonPathStringValue("$.fieldErrors[0].property").isEqualTo("testField");
        assertThat(content).extractingJsonPathStringValue("$.fieldErrors[0].message").isEqualTo("Test Field Message");
        assertThat(content).extractingJsonPathValue("$.fieldErrors[0].rejectedValue").isNull();
        assertThat(content).extractingJsonPathStringValue("$.fieldErrors[0].path").isEqualTo("path");
    }

    @Test
    void testSerializationWithGlobalError() throws IOException {
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message");
        response.addGlobalError(new ApiGlobalError("GLOBAL_ERROR_CODE", "Test Global Message"));
        JsonContent<ApiErrorResponse> content = tester.write(response);
        assertThat(content).extractingJsonPathStringValue("$.code").isEqualTo("TEST_CODE");
        assertThat(content).extractingJsonPathStringValue("$.message").isEqualTo("Test message");
        assertThat(content).doesNotHaveJsonPath("$.httpStatus");
        assertThat(content).extractingJsonPathStringValue("$.globalErrors[0].code").isEqualTo("GLOBAL_ERROR_CODE");
        assertThat(content).extractingJsonPathStringValue("$.globalErrors[0].message").isEqualTo("Test Global Message");
    }

    @Test
    void testSerializationWithErrorProperty() throws IOException {
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message");
        response.addErrorProperty("property1", "stringValue");
        response.addErrorProperty("property2", 15);
        JsonContent<ApiErrorResponse> content = tester.write(response);
        assertThat(content).extractingJsonPathStringValue("$.code").isEqualTo("TEST_CODE");
        assertThat(content).extractingJsonPathStringValue("$.message").isEqualTo("Test message");
        assertThat(content).doesNotHaveJsonPath("$.httpStatus");
        assertThat(content).extractingJsonPathStringValue("$.property1").isEqualTo("stringValue");
        assertThat(content).extractingJsonPathNumberValue("$.property2").isEqualTo(15);
    }

    @Test
    void testSerializationWithErrorPropertyThatIsNull() throws IOException {
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message");
        response.addErrorProperty("property1", null);
        JsonContent<ApiErrorResponse> content = tester.write(response);
        assertThat(content).extractingJsonPathStringValue("$.code").isEqualTo("TEST_CODE");
        assertThat(content).extractingJsonPathStringValue("$.message").isEqualTo("Test message");
        assertThat(content).doesNotHaveJsonPath("$.httpStatus");
        assertThat(content).extractingJsonPathValue("$.property1").isNull();
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testHttpStatusInJsonResponse() throws IOException {
        properties.setHttpStatusInJsonResponse(true);
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_REQUEST, "TEST_CODE", "Test message");

        JsonContent<ApiErrorResponse> content = tester.write(response);
        assertThat(content).extractingJsonPathNumberValue("$.status").isEqualTo(400);
    }

    @Test
    void testHttpStatusInJsonResponseDisabledByDefault() throws IOException {
        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_REQUEST, "TEST_CODE", "Test message");

        JsonContent<ApiErrorResponse> content = tester.write(response);
        assertThat(content).doesNotHaveJsonPath("$.status");
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
            JsonContent<ApiErrorResponse> content = tester.write(new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message"));
            assertThat(content).extractingJsonPathStringValue("$.errorCode").isEqualTo("TEST_CODE");
            assertThat(content).extractingJsonPathStringValue("$.description").isEqualTo("Test message");
            assertThat(content).doesNotHaveJsonPath("$.httpStatus");
        }

        @Test
        void testSerializationWithFieldError() throws IOException {
            ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message");
            response.addFieldError(new ApiFieldError("FIELD_ERROR_CODE", "testField", "Test Field Message", "bad", "path"));
            JsonContent<ApiErrorResponse> content = tester.write(response);
            assertThat(content).extractingJsonPathStringValue("$.errorCode").isEqualTo("TEST_CODE");
            assertThat(content).extractingJsonPathStringValue("$.description").isEqualTo("Test message");
            assertThat(content).doesNotHaveJsonPath("$.httpStatus");
            assertThat(content).extractingJsonPathStringValue("$.fieldFailures[0].errorCode").isEqualTo("FIELD_ERROR_CODE");
            assertThat(content).extractingJsonPathStringValue("$.fieldFailures[0].property").isEqualTo("testField");
            assertThat(content).extractingJsonPathStringValue("$.fieldFailures[0].description").isEqualTo("Test Field Message");
            assertThat(content).extractingJsonPathStringValue("$.fieldFailures[0].rejectedValue").isEqualTo("bad");
            assertThat(content).extractingJsonPathStringValue("$.fieldFailures[0].path").isEqualTo("path");
        }

        @Test
        void testSerializationWithGlobalError() throws IOException {
            ApiErrorResponse response = new ApiErrorResponse(HttpStatus.BAD_GATEWAY, "TEST_CODE", "Test message");
            response.addGlobalError(new ApiGlobalError("GLOBAL_ERROR_CODE", "Test Global Message"));
            JsonContent<ApiErrorResponse> content = tester.write(response);
            assertThat(content).extractingJsonPathStringValue("$.errorCode").isEqualTo("TEST_CODE");
            assertThat(content).extractingJsonPathStringValue("$.description").isEqualTo("Test message");
            assertThat(content).doesNotHaveJsonPath("$.httpStatus");
            assertThat(content).extractingJsonPathStringValue("$.globalFailures[0].errorCode").isEqualTo("GLOBAL_ERROR_CODE");
            assertThat(content).extractingJsonPathStringValue("$.globalFailures[0].description").isEqualTo("Test Global Message");
        }

    }
}
