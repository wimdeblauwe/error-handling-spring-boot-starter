package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@Import(ErrorHandlingProperties.class)
class ApiErrorResponseDeserializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void codeAndMessage() {
        var json = """
            {
              "code": "USER_NOT_FOUND",
              "message": "Could not find user with id 123"
            }""";

        var response = objectMapper.readValue(json, ApiErrorResponse.class);

        assertThat(response).extracting(
                ApiErrorResponse::getCode,
                ApiErrorResponse::getMessage,
                ApiErrorResponse::getHttpStatus,
                ApiErrorResponse::getFieldErrors,
                ApiErrorResponse::getGlobalErrors,
                ApiErrorResponse::getParameterErrors,
                ApiErrorResponse::getProperties)
            .containsExactly(
                "USER_NOT_FOUND",
                "Could not find user with id 123",
                null,
                List.of(),
                List.of(),
                List.of(),
                Map.of()
            );
    }

    @Test
    void httpStatus() {
        var json = """
            {
              "status": 404,
              "code": "USER_NOT_FOUND",
              "message": "Could not find user with id 123"
            }""";
        var response = objectMapper.readValue(json, ApiErrorResponse.class);

        assertThat(response).extracting(
                ApiErrorResponse::getCode,
                ApiErrorResponse::getMessage,
                ApiErrorResponse::getHttpStatus)
            .containsExactly("USER_NOT_FOUND", "Could not find user with id 123", HttpStatusCode.valueOf(404));
    }

    @Test
    void singleFieldError() {
        var json = """
            {
              "code": "VALIDATION_FAILED",
              "message": "Validation failed for object='exampleRequestBody'",
              "fieldErrors": [
                {
                  "code": "INVALID_SIZE",
                  "property": "name",
                  "message": "size must be between 10 and 2147483647",
                  "rejectedValue": "",
                  "path": "name"
                }
              ]
            }""";

        var fieldErrors = objectMapper.readValue(json, ApiErrorResponse.class).getFieldErrors();
        assertThat(fieldErrors).singleElement()
            .extracting(
                ApiFieldError::getCode,
                ApiFieldError::getMessage,
                ApiFieldError::getProperty,
                ApiFieldError::getRejectedValue,
                ApiFieldError::getPath
            )
            .containsExactly("INVALID_SIZE", "size must be between 10 and 2147483647", "name", "", "name");
    }

    @Test
    void rejectedFieldValueTypes() {
        var json = """
            {
              "code": "VALIDATION_FAILED",
              "message": "Validation failed for object='exampleRequestBody'",
              "fieldErrors": [
                {
                  "code": "INVALID",
                  "property": "stringName",
                  "message": "invalid value",
                  "rejectedValue": "string",
                  "path": "stringName"
                },
                {
                  "code": "INVALID",
                  "property": "nullName",
                  "message": "invalid value",
                  "rejectedValue": null,
                  "path": "nullName"
                },
                {
                  "code": "INVALID",
                  "property": "intName",
                  "message": "invalid value",
                  "rejectedValue": 66,
                  "path": "intName"
                },
                {
                  "code": "INVALID",
                  "property": "longName",
                  "message": "invalid value",
                  "rejectedValue": 3147483647,
                  "path": "longName"
                },
                {
                  "code": "INVALID",
                  "property": "decimalName",
                  "message": "invalid value",
                  "rejectedValue": 66.6,
                  "path": "decimalName"
                },
                {
                  "code": "INVALID",
                  "property": "booleanName",
                  "message": "invalid value",
                  "rejectedValue": true,
                  "path": "booleanName"
                },
                {
                  "code": "INVALID",
                  "property": "arrayType",
                  "message": "invalid value",
                  "rejectedValue": [99],
                  "path": "arrayType"
                },
                {
                  "code": "INVALID",
                  "property": "objectType",
                  "message": "invalid value",
                  "rejectedValue": {"foo": "bar"},
                  "path": "objectType"
                }
              ]
            }""";

        var fieldErrors = objectMapper.readValue(json, ApiErrorResponse.class).getFieldErrors();
        assertThat(fieldErrors).hasSize(8)
            .extracting(ApiFieldError::getRejectedValue)
            .containsExactly(
                "string",
                null,
                66,
                3_147_483_647L,
                66.6,
                true,
                List.of(99),
                Map.of("foo", "bar")
            );
    }

    @Test
    void singleGlobalError() {
        var json = """
            {
              "code": "VALIDATION_FAILED",
              "message": "Validation failed for object='exampleRequestBody'. Error count: 4",
              "globalErrors": [
                {
                  "code": "ValidCustomer",
                  "message": "Invalid customer"
                }
              ]
            }""";
        var globalErrors = objectMapper.readValue(json, ApiErrorResponse.class).getGlobalErrors();
        assertThat(globalErrors).singleElement()
            .extracting(ApiGlobalError::getCode, ApiGlobalError::getMessage)
            .containsExactly("ValidCustomer", "Invalid customer");
    }

    @Test
    void singleParameterError() {
        var json = """
            {
              "code": "VALIDATION_FAILED",
              "message": "Validation failed for object='exampleRequestBody'. Error count: 4",
              "parameterErrors": [
                {
                  "code": "REQUIRED_NOT_NULL",
                  "message": "must not be null",
                  "parameter": "extraArg",
                  "rejectedValue": null
                }
              ]
            }""";

        var parameterErrors = objectMapper.readValue(json, ApiErrorResponse.class).getParameterErrors();
        assertThat(parameterErrors).singleElement()
            .extracting(
                ApiParameterError::getCode,
                ApiParameterError::getMessage,
                ApiParameterError::getParameter,
                ApiParameterError::getRejectedValue
            )
            .containsExactly("REQUIRED_NOT_NULL", "must not be null", "extraArg", null);
    }

    @Test
    void extraResponseProperties() {
        var json = """
            {
              "code": "USER_NOT_FOUND",
              "message": "Could not find user with id UserId{id=8c7fb13c-0924-47d4-821a-36f73558c898}",
              "extraStringProp": "8c7fb13c-0924-47d4-821a-36f73558c898",
              "extraBooleanProp": false,
              "extraIntProp": 66,
              "extraLongProp": 3147483647,
              "extraDecimalProp": 66.6
            }""";

        var extraResponseProperties = objectMapper.readValue(json, ApiErrorResponse.class).getProperties();
        assertThat(extraResponseProperties).containsExactlyInAnyOrderEntriesOf(Map.of(
            "extraStringProp", "8c7fb13c-0924-47d4-821a-36f73558c898",
            "extraBooleanProp", false,
            "extraIntProp", 66,
            "extraLongProp", 3_147_483_647L,
            "extraDecimalProp", 66.6
        ));
    }
}
