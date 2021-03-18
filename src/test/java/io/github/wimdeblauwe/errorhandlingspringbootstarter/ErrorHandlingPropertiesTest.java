package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = ErrorHandlingProperties.class)
@TestPropertySource("error-handling-properties-test.properties")
class ErrorHandlingPropertiesTest {

    @Autowired
    private ErrorHandlingProperties properties;

    @Test
    void loadProperties() {
        assertThat(properties).isNotNull();
        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getJsonFieldNames().getCode()).isEqualTo("kode");
        assertThat(properties.getJsonFieldNames().getMessage()).isEqualTo("description");
        assertThat(properties.getJsonFieldNames().getFieldErrors()).isEqualTo("veldFouten");
        assertThat(properties.getJsonFieldNames().getGlobalErrors()).isEqualTo("globaleFouten");
        assertThat(properties.getExceptionLogging()).isEqualTo(ErrorHandlingProperties.ExceptionLogging.WITH_STACKTRACE);
        assertThat(properties.getDefaultErrorCodeStrategy()).isEqualTo(ErrorHandlingProperties.DefaultErrorCodeStrategy.ALL_CAPS_CONVERSION);
        assertThat(properties.getHttpStatuses())
                .hasSize(1)
                .hasEntrySatisfying("java.lang.IllegalArgumentException", httpStatus -> assertThat(httpStatus).isEqualTo(HttpStatus.BAD_REQUEST));
        assertThat(properties.getCodes())
                .hasSize(1)
                .hasEntrySatisfying("java.lang.NullPointerException", code -> assertThat(code).isEqualTo("NPE"));
        assertThat(properties.getMessages())
                .hasSize(1)
                .hasEntrySatisfying("java.lang.NullPointerException", message -> assertThat(message).isEqualTo("A null pointer was thrown!"));
    }

}
