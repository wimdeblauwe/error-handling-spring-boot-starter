package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import org.assertj.core.api.HamcrestCondition;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class DefaultFallbackApiExceptionHandlerTest {

    @Nested
    class HttpStatusTests {
        @Test
        void defaultHttpStatus() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = new DefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new MyEntityNotFoundException());
            assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @Test
        void overrideViaAnnotation() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = new DefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new ExceptionWithBadRequestStatus());
            assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void overrideViaProperties() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            MyEntityNotFoundException exception = new MyEntityNotFoundException();
            properties.getHttpStatuses().put(exception.getClass().getName(), HttpStatus.BAD_REQUEST);
            DefaultFallbackApiExceptionHandler handler = new DefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(exception);
            assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

    }

    @Nested
    class CodeTests {
        @Test
        void codeUsesResponseErrorCodeAnnotation() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = new DefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new ExceptionWithResponseErrorCode());
            assertThat(response.getCode()).isEqualTo("MY_ERROR_CODE");
        }

        @Test
        void codeUsesFqnWhenNoResponseErrorCodeAnnotation() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            DefaultFallbackApiExceptionHandler handler = new DefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new MyEntityNotFoundException());
            assertThat(response.getCode()).isEqualTo("io.github.wimdeblauwe.errorhandlingspringbootstarter.DefaultFallbackApiExceptionHandlerTest$MyEntityNotFoundException");
        }

        @Test
        void codeUsesAllCapsWhenConfigured() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            properties.setDefaultErrorCodeStrategy(ErrorHandlingProperties.DefaultErrorCodeStrategy.ALL_CAPS_CONVERSION);
            DefaultFallbackApiExceptionHandler handler = new DefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new MyEntityNotFoundException());
            assertThat(response.getCode()).isEqualTo("MY_ENTITY_NOT_FOUND");
        }

        @Test
        void codeUsesOverrideAlways() {
            ErrorHandlingProperties properties = new ErrorHandlingProperties();
            properties.setDefaultErrorCodeStrategy(ErrorHandlingProperties.DefaultErrorCodeStrategy.ALL_CAPS_CONVERSION);
            Map<String, String> codes = new HashMap<>();
            codes.put("io.github.wimdeblauwe.errorhandlingspringbootstarter.DefaultFallbackApiExceptionHandlerTest$MyEntityNotFoundException", "MY_CUSTOM_ERROR_CODE");
            properties.setCodes(codes);
            DefaultFallbackApiExceptionHandler handler = new DefaultFallbackApiExceptionHandler(properties);
            ApiErrorResponse response = handler.handle(new MyEntityNotFoundException());
            assertThat(response.getCode()).isEqualTo("MY_CUSTOM_ERROR_CODE");
        }
    }

    @Test
    void testResponseErrorPropertyOnField() {
        ErrorHandlingProperties properties = new ErrorHandlingProperties();
        DefaultFallbackApiExceptionHandler handler = new DefaultFallbackApiExceptionHandler(properties);
        ApiErrorResponse response = handler.handle(new ExceptionWithResponseErrorPropertyOnField("myValue"));
        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getCode()).isEqualTo(ExceptionWithResponseErrorPropertyOnField.class.getName());
        assertThat(response.getMessage()).isNull();
        assertThat(response.getProperties()).hasEntrySatisfying("myProperty", new HamcrestCondition<>(Matchers.is("myValue")));
    }

    @Test
    void testResponseErrorPropertyOnFieldWithMessage() {
        ErrorHandlingProperties properties = new ErrorHandlingProperties();
        DefaultFallbackApiExceptionHandler handler = new DefaultFallbackApiExceptionHandler(properties);
        ApiErrorResponse response = handler.handle(new ExceptionWithResponseErrorPropertyOnField("This is an exceptional case.", "myValue"));
        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getCode()).isEqualTo(ExceptionWithResponseErrorPropertyOnField.class.getName());
        assertThat(response.getMessage()).isEqualTo("This is an exceptional case.");
        assertThat(response.getProperties()).hasEntrySatisfying("myProperty", new HamcrestCondition<>(Matchers.is("myValue")));
    }

    @Test
    void testResponseErrorPropertyOnFieldWithNullValue() {
        ErrorHandlingProperties properties = new ErrorHandlingProperties();
        DefaultFallbackApiExceptionHandler handler = new DefaultFallbackApiExceptionHandler(properties);
        ApiErrorResponse response = handler.handle(new ExceptionWithResponseErrorPropertyOnField(null));
        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getCode()).isEqualTo(ExceptionWithResponseErrorPropertyOnField.class.getName());
        assertThat(response.getMessage()).isNull();
        assertThat(response.getProperties()).doesNotContainKey("myProperty");
    }

    @Test
    void testResponseErrorPropertyOnFieldWithNullValueIncluded() {
        ErrorHandlingProperties properties = new ErrorHandlingProperties();
        DefaultFallbackApiExceptionHandler handler = new DefaultFallbackApiExceptionHandler(properties);
        ApiErrorResponse response = handler.handle(new ExceptionWithResponseErrorPropertyOnFieldWithIncludeIfNull(null));
        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getCode()).isEqualTo(ExceptionWithResponseErrorPropertyOnFieldWithIncludeIfNull.class.getName());
        assertThat(response.getMessage()).isNull();
        assertThat(response.getProperties()).hasEntrySatisfying("myProperty", new HamcrestCondition<>(Matchers.nullValue()));
    }

    @Test
    void testResponseErrorPropertyOnMethod() {
        ErrorHandlingProperties properties = new ErrorHandlingProperties();
        DefaultFallbackApiExceptionHandler handler = new DefaultFallbackApiExceptionHandler(properties);
        ApiErrorResponse response = handler.handle(new ExceptionWithResponseErrorPropertyOnMethod("myValue"));
        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getCode()).isEqualTo(ExceptionWithResponseErrorPropertyOnMethod.class.getName());
        assertThat(response.getMessage()).isNull();
        assertThat(response.getProperties()).hasEntrySatisfying("myProperty", new HamcrestCondition<>(Matchers.is("myValue")));
    }

    @Test
    void testResponseErrorPropertyOnMethodWithMessage() {
        ErrorHandlingProperties properties = new ErrorHandlingProperties();
        DefaultFallbackApiExceptionHandler handler = new DefaultFallbackApiExceptionHandler(properties);
        ApiErrorResponse response = handler.handle(new ExceptionWithResponseErrorPropertyOnMethod("This is an exceptional case.", "myValue"));
        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getCode()).isEqualTo(ExceptionWithResponseErrorPropertyOnMethod.class.getName());
        assertThat(response.getMessage()).isEqualTo("This is an exceptional case.");
        assertThat(response.getProperties()).hasEntrySatisfying("myProperty", new HamcrestCondition<>(Matchers.is("myValue")));
    }

    @Test
    void testResponseErrorPropertyOnMethodWithNullValue() {
        ErrorHandlingProperties properties = new ErrorHandlingProperties();
        DefaultFallbackApiExceptionHandler handler = new DefaultFallbackApiExceptionHandler(properties);
        ApiErrorResponse response = handler.handle(new ExceptionWithResponseErrorPropertyOnMethod(null));
        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getCode()).isEqualTo(ExceptionWithResponseErrorPropertyOnMethod.class.getName());
        assertThat(response.getMessage()).isNull();
        assertThat(response.getProperties()).doesNotContainKey("myProperty");
    }

    @Test
    void testResponseErrorPropertyOnMethodWithNullValueIncluded() {
        ErrorHandlingProperties properties = new ErrorHandlingProperties();
        DefaultFallbackApiExceptionHandler handler = new DefaultFallbackApiExceptionHandler(properties);
        ApiErrorResponse response = handler.handle(new ExceptionWithResponseErrorPropertyOnMethodWithIncludeIfNull(null));
        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getCode()).isEqualTo(ExceptionWithResponseErrorPropertyOnMethodWithIncludeIfNull.class.getName());
        assertThat(response.getMessage()).isNull();
        assertThat(response.getProperties()).hasEntrySatisfying("myProperty", new HamcrestCondition<>(Matchers.nullValue()));
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
