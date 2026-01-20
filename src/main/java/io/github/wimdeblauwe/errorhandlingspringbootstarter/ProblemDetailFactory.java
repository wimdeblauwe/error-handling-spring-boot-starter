package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Component
public class ProblemDetailFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProblemDetailFactory.class);

    private final ErrorHandlingProperties properties;

    public ProblemDetailFactory(ErrorHandlingProperties properties) {
        this.properties = properties;
    }

    public ProblemDetail build(ApiErrorResponse errorResponse) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(Objects.requireNonNull(errorResponse.getHttpStatus()), errorResponse.getMessage());
        problemDetail.setDetail(errorResponse.getMessage());
        try {
            problemDetail.setType(new URI(properties.getProblemDetailTypePrefix() + toKebabCase(errorResponse.getCode())));
        } catch (URISyntaxException ignored) {
        }

        HashMap<String, Object> allProperties = new HashMap<>();
        allProperties.putAll(errorResponse.getProperties());
        List<ApiFieldError> fieldErrors = errorResponse.getFieldErrors();
        if (!fieldErrors.isEmpty()) {
            allProperties.put("fieldErrors", fieldErrors);
        }
        List<ApiGlobalError> globalErrors = errorResponse.getGlobalErrors();
        if (!globalErrors.isEmpty()) {
            allProperties.put("globalErrors", globalErrors);
        }
        problemDetail.setProperties(allProperties);

        return problemDetail;
    }

    private String toKebabCase(String input) {
        if (input.isEmpty()) {
            return input;
        }

        String result = input
                .replaceAll("([a-z])([A-Z])", "$1-$2")  // camelCase boundaries
                .replaceAll("([A-Z])([A-Z][a-z])", "$1-$2")  // handle acronyms
                .replaceAll("\\s+", "-")  // spaces to hyphens
                .replaceAll("_", "-")  // underscores to hyphens
                .toLowerCase();
        LOGGER.info("{} -> {}", input, result);
        return result;
    }
}
