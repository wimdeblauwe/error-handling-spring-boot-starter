package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import org.assertj.core.api.HamcrestCondition;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;


class DefaultFallbackApiExceptionHandlerTest {

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
