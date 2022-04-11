package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties("error.handling")
@Component
public class ErrorHandlingProperties {
    private boolean enabled = true;

    private JsonFieldNames jsonFieldNames = new JsonFieldNames();

    private ExceptionLogging exceptionLogging = ExceptionLogging.MESSAGE_ONLY;

    private List<Class<? extends Throwable>> fullStacktraceClasses = new ArrayList<>();

    private List<String> fullStacktraceHttpStatuses = new ArrayList<>();

    private DefaultErrorCodeStrategy defaultErrorCodeStrategy = DefaultErrorCodeStrategy.ALL_CAPS;

    private boolean httpStatusInJsonResponse = false;

    private Map<String, HttpStatus> httpStatuses = new HashMap<>();

    private Map<String, String> codes = new HashMap<>();

    private Map<String, String> messages = new HashMap<>();

    private boolean searchSuperClassHierarchy = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public JsonFieldNames getJsonFieldNames() {
        return jsonFieldNames;
    }

    public void setJsonFieldNames(JsonFieldNames jsonFieldNames) {
        this.jsonFieldNames = jsonFieldNames;
    }

    public ExceptionLogging getExceptionLogging() {
        return exceptionLogging;
    }

    public void setExceptionLogging(ExceptionLogging exceptionLogging) {
        this.exceptionLogging = exceptionLogging;
    }

    public List<Class<? extends Throwable>> getFullStacktraceClasses() {
        return fullStacktraceClasses;
    }

    public void setFullStacktraceClasses(List<Class<? extends Throwable>> fullStacktraceClasses) {
        this.fullStacktraceClasses = fullStacktraceClasses;
    }

    public List<String> getFullStacktraceHttpStatuses() {
        return fullStacktraceHttpStatuses;
    }

    public void setFullStacktraceHttpStatuses(List<String> fullStacktraceHttpStatuses) {
        this.fullStacktraceHttpStatuses = fullStacktraceHttpStatuses;
    }

    public DefaultErrorCodeStrategy getDefaultErrorCodeStrategy() {
        return defaultErrorCodeStrategy;
    }

    public void setDefaultErrorCodeStrategy(DefaultErrorCodeStrategy defaultErrorCodeStrategy) {
        this.defaultErrorCodeStrategy = defaultErrorCodeStrategy;
    }

    public boolean isHttpStatusInJsonResponse() {
        return httpStatusInJsonResponse;
    }

    public void setHttpStatusInJsonResponse(boolean httpStatusInJsonResponse) {
        this.httpStatusInJsonResponse = httpStatusInJsonResponse;
    }

    public Map<String, HttpStatus> getHttpStatuses() {
        return httpStatuses;
    }

    public void setHttpStatuses(Map<String, HttpStatus> httpStatuses) {
        this.httpStatuses = httpStatuses;
    }

    public Map<String, String> getCodes() {
        return codes;
    }

    public void setCodes(Map<String, String> codes) {
        this.codes = codes;
    }

    public Map<String, String> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, String> messages) {
        this.messages = messages;
    }

    public boolean isSearchSuperClassHierarchy() {
        return searchSuperClassHierarchy;
    }

    public void setSearchSuperClassHierarchy(boolean searchSuperClassHierarchy) {
        this.searchSuperClassHierarchy = searchSuperClassHierarchy;
    }

    public enum ExceptionLogging {
        NO_LOGGING,
        MESSAGE_ONLY,
        WITH_STACKTRACE
    }

    public enum DefaultErrorCodeStrategy {
        FULL_QUALIFIED_NAME,
        ALL_CAPS
    }

    public static class JsonFieldNames {
        private String code = "code";
        private String message = "message";
        private String fieldErrors = "fieldErrors";
        private String globalErrors = "globalErrors";
        private String parameterErrors = "parameterErrors";

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getFieldErrors() {
            return fieldErrors;
        }

        public void setFieldErrors(String fieldErrors) {
            this.fieldErrors = fieldErrors;
        }

        public String getGlobalErrors() {
            return globalErrors;
        }

        public void setGlobalErrors(String globalErrors) {
            this.globalErrors = globalErrors;
        }

        public String getParameterErrors() {
            return parameterErrors;
        }

        public void setParameterErrors(String parameterErrors) {
            this.parameterErrors = parameterErrors;
        }
    }
}
