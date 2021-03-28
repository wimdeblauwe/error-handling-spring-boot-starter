package io.github.wimdeblauwe.errorhandlingspringbootstarter.handler;

import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component //<.>
public class CustomExceptionApiExceptionHandler implements ApiExceptionHandler { //<.>
    @Override
    public boolean canHandle(Throwable exception) {
        return exception instanceof CustomException; //<.>
    }

    @Override
    public ApiErrorResponse handle(Throwable exception) {
        CustomException customException = (CustomException) exception;

        ApiErrorResponse response = new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, //<.>
                                                         "MY_CUSTOM_EXCEPTION",
                                                         exception.getMessage());
        Throwable cause = customException.getCause();
        Map<String, Object> nestedCause = new HashMap<>();
        nestedCause.put("code", "CAUSE");
        nestedCause.put("message", cause.getMessage());
        response.addErrorProperty("cause", nestedCause); //<.>

        return response;
    }
}
