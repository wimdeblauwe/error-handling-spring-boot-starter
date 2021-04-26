package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import org.assertj.core.api.HamcrestCondition;
import org.assertj.core.util.Lists;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class DefaultFallbackApiExceptionHandlerTest {

    @Nested
    class HttpStatusTests {
        @Test
        void defaultHttpStatus() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new MyEntityNotFoundException());
            assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        void overrideViaAnnotation() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new ExceptionWithBadRequestStatus());
            assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void overrideViaProperties() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            MyEntityNotFoundException exception = new MyEntityNotFoundException();
            properties.getHttpStatuses().put(exception.getClass().getName(), HttpStatus.BAD_REQUEST);
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(exception);
            assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void propertiesHavePrecedenceOnAnnotation() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            ExceptionWithBadRequestStatus exception = new ExceptionWithBadRequestStatus();
            properties.getHttpStatuses().put(exception.getClass().getName(), HttpStatus.I_AM_A_TEAPOT);
            ApiErrorResponse response = handler.handle(exception);
            assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.I_AM_A_TEAPOT);
        }
    }

    @Nested
    class CodeTests {
        @Test
        void codeUsesResponseErrorCodeAnnotation() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new ExceptionWithResponseErrorCode());
            assertThat(response.getCode()).isEqualTo("MY_ERROR_CODE");
        }

        @Test
        void codeUsesDefaultWhenNoResponseErrorCodeAnnotation() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new MyEntityNotFoundException());
            assertThat(response.getCode()).isEqualTo("MY_ENTITY_NOT_FOUND");
        }

        @Test
        void codeUsesAllCapsWhenConfigured() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            properties.setDefaultErrorCodeStrategy(ErrorHandlingProperties.DefaultErrorCodeStrategy.ALL_CAPS);
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new MyEntityNotFoundException());
            assertThat(response.getCode()).isEqualTo("MY_ENTITY_NOT_FOUND");
        }

        @Test
        void codeUsesOverrideAlways() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            properties.setDefaultErrorCodeStrategy(ErrorHandlingProperties.DefaultErrorCodeStrategy.ALL_CAPS);
            Map<String, String> codes = new HashMap<>();
            codes.put("io.github.wimdeblauwe.errorhandlingspringbootstarter.DefaultFallbackApiExceptionHandlerTest$MyEntityNotFoundException", "MY_CUSTOM_ERROR_CODE");
            properties.setCodes(codes);
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new MyEntityNotFoundException());
            assertThat(response.getCode()).isEqualTo("MY_CUSTOM_ERROR_CODE");
        }

        @Test
        void propertiesHavePrecedenceOnAnnotation() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            ExceptionWithResponseErrorCode exception = new ExceptionWithResponseErrorCode();
            properties.getCodes().put(exception.getClass().getName(), "CODE_VIA_PROPERTIES");
            ApiErrorResponse response = handler.handle(exception);
            assertThat(response.getCode()).isEqualTo("CODE_VIA_PROPERTIES");
        }
    }

    @Nested
    class MessageTests {
        @Test
        void messageUsesExceptionMessage() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new RuntimeException("This is the exception message"));
            assertThat(response.getMessage()).isEqualTo("This is the exception message");
        }

        @Test
        void propertiesHavePrecedenceOnExceptionMessage() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            RuntimeException exception = new RuntimeException("This is the exception message");
            properties.getMessages().put(exception.getClass().getName(), "This is the exception message via properties");
            ApiErrorResponse response = handler.handle(exception);
            assertThat(response.getMessage()).isEqualTo("This is the exception message via properties");
        }
    }

    @Nested
    class ResponseErrorPropertyOnFieldTests {
        @Test
        void testResponseErrorPropertyOnField() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new ExceptionWithResponseErrorPropertyOnField("myValue"));
            assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getCode()).isEqualTo("EXCEPTION_WITH_RESPONSE_ERROR_PROPERTY_ON_FIELD");
            assertThat(response.getMessage()).isNull();
            assertThat(response.getProperties()).hasEntrySatisfying("myProperty", new HamcrestCondition<>(Matchers.is("myValue")));
        }

        @Test
        void testResponseErrorPropertyOnFieldWithMessage() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new ExceptionWithResponseErrorPropertyOnField("This is an exceptional case.", "myValue"));
            assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getCode()).isEqualTo("EXCEPTION_WITH_RESPONSE_ERROR_PROPERTY_ON_FIELD");
            assertThat(response.getMessage()).isEqualTo("This is an exceptional case.");
            assertThat(response.getProperties()).hasEntrySatisfying("myProperty", new HamcrestCondition<>(Matchers.is("myValue")));
        }

        @Test
        void testResponseErrorPropertyOnFieldWithNullValue() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new ExceptionWithResponseErrorPropertyOnField(null));
            assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getCode()).isEqualTo("EXCEPTION_WITH_RESPONSE_ERROR_PROPERTY_ON_FIELD");
            assertThat(response.getMessage()).isNull();
            assertThat(response.getProperties()).doesNotContainKey("myProperty");
        }

        @Test
        void testResponseErrorPropertyOnFieldWithNullValueIncluded() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new ExceptionWithResponseErrorPropertyOnFieldWithIncludeIfNull(null));
            assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getCode()).isEqualTo("EXCEPTION_WITH_RESPONSE_ERROR_PROPERTY_ON_FIELD_WITH_INCLUDE_IF_NULL");
            assertThat(response.getMessage()).isNull();
            assertThat(response.getProperties()).hasEntrySatisfying("myProperty", new HamcrestCondition<>(Matchers.nullValue()));
        }
    }

    @Nested
    class ResponseErrorPropertyOnMethodTests {
        @Test
        void testResponseErrorPropertyOnMethod() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new ExceptionWithResponseErrorPropertyOnMethod("myValue"));
            assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getCode()).isEqualTo("EXCEPTION_WITH_RESPONSE_ERROR_PROPERTY_ON_METHOD");
            assertThat(response.getMessage()).isNull();
            assertThat(response.getProperties()).hasEntrySatisfying("myProperty", new HamcrestCondition<>(Matchers.is("myValue")));
        }

        @Test
        void testResponseErrorPropertyOnMethodWithMessage() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new ExceptionWithResponseErrorPropertyOnMethod("This is an exceptional case.", "myValue"));
            assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getCode()).isEqualTo("EXCEPTION_WITH_RESPONSE_ERROR_PROPERTY_ON_METHOD");
            assertThat(response.getMessage()).isEqualTo("This is an exceptional case.");
            assertThat(response.getProperties()).hasEntrySatisfying("myProperty", new HamcrestCondition<>(Matchers.is("myValue")));
        }

        @Test
        void testResponseErrorPropertyOnMethodWithNullValue() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new ExceptionWithResponseErrorPropertyOnMethod(null));
            assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getCode()).isEqualTo("EXCEPTION_WITH_RESPONSE_ERROR_PROPERTY_ON_METHOD");
            assertThat(response.getMessage()).isNull();
            assertThat(response.getProperties()).doesNotContainKey("myProperty");
        }

        @Test
        void testResponseErrorPropertyOnMethodWithNullValueIncluded() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new ExceptionWithResponseErrorPropertyOnMethodWithIncludeIfNull(null));
            assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getCode()).isEqualTo("EXCEPTION_WITH_RESPONSE_ERROR_PROPERTY_ON_METHOD_WITH_INCLUDE_IF_NULL");
            assertThat(response.getMessage()).isNull();
            assertThat(response.getProperties()).hasEntrySatisfying("myProperty", new HamcrestCondition<>(Matchers.nullValue()));
        }
    }

    @Test
    void testResponseStatusForResponseStatusException() {
        ErrorHandlingProperties properties = new ErrorHandlingProperties();
        DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
        ApiErrorResponse response = handler.handle(new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, "Test of a response status exception message"));
        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.I_AM_A_TEAPOT);
        assertThat(response.getCode()).isEqualTo("RESPONSE_STATUS");
        assertThat(response.getMessage()).isEqualTo("418 I_AM_A_TEAPOT \"Test of a response status exception message\"");
    }

    @Test
    void testResponseStatusForMethodNotAllowedException() {
        ErrorHandlingProperties properties = new ErrorHandlingProperties();
        DefaultFallbackApiExceptionHandler handler = createDefaultFallbackApiExceptionHandler(properties);
        ApiErrorResponse response = handler.handle(new MethodNotAllowedException(HttpMethod.OPTIONS, Lists.newArrayList(HttpMethod.GET, HttpMethod.POST)));
        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getCode()).isEqualTo("METHOD_NOT_ALLOWED");
        assertThat(response.getMessage()).isEqualTo("405 METHOD_NOT_ALLOWED \"Request method 'OPTIONS' not supported\"");
    }

    private DefaultFallbackApiExceptionHandler createDefaultFallbackApiExceptionHandler(ErrorHandlingProperties properties) {
        return new DefaultFallbackApiExceptionHandler(new HttpStatusMapper(properties),
                                                      new ErrorCodeMapper(properties),
                                                      new ErrorMessageMapper(properties));
    }

    static class MyEntityNotFoundException extends RuntimeException {

    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    static class ExceptionWithBadRequestStatus extends RuntimeException {

    }

    @ResponseErrorCode("MY_ERROR_CODE")
    static class ExceptionWithResponseErrorCode extends RuntimeException {

    }

    static class ExceptionWithResponseErrorPropertyOnField extends RuntimeException {
        @ResponseErrorProperty
        private final String myProperty;

        public ExceptionWithResponseErrorPropertyOnField(String message, String myProperty) {
            super(message);
            this.myProperty = myProperty;
        }

        public ExceptionWithResponseErrorPropertyOnField(String myProperty) {
            this.myProperty = myProperty;
        }

        public String getMyProperty() {
            return myProperty;
        }
    }

    static class ExceptionWithResponseErrorPropertyOnFieldWithIncludeIfNull extends RuntimeException {
        @ResponseErrorProperty(includeIfNull = true)
        private final String myProperty;

        public ExceptionWithResponseErrorPropertyOnFieldWithIncludeIfNull(String myProperty) {
            this.myProperty = myProperty;
        }

        public String getMyProperty() {
            return myProperty;
        }
    }

    static class ExceptionWithResponseErrorPropertyOnMethod extends RuntimeException {
        private final String myProperty;

        public ExceptionWithResponseErrorPropertyOnMethod(String message, String myProperty) {
            super(message);
            this.myProperty = myProperty;
        }

        public ExceptionWithResponseErrorPropertyOnMethod(String myProperty) {
            this.myProperty = myProperty;
        }

        @ResponseErrorProperty
        public String getMyProperty() {
            return myProperty;
        }
    }

    static class ExceptionWithResponseErrorPropertyOnMethodWithIncludeIfNull extends RuntimeException {
        private final String myProperty;

        public ExceptionWithResponseErrorPropertyOnMethodWithIncludeIfNull(String myProperty) {
            this.myProperty = myProperty;
        }

        @ResponseErrorProperty(includeIfNull = true)
        public String getMyProperty() {
            return myProperty;
        }
    }
}
