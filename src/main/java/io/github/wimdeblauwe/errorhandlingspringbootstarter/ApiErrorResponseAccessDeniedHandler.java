package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Use this {@link AccessDeniedHandler} implementation if you want to have a consistent response
 * with how this library works when the user is not allowed to access a resource.
 * <p>
 * It is impossible for the library to provide auto-configuration for this. So you need to manually add
 * this to your security configuration. For example:
 *
 * <pre>
 *     public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {*
 *         &#64;Bean
 *         public AccessDeniedHandler accessDeniedHandler(HttpStatusMapper httpStatusMapper, ErrorCodeMapper errorCodeMapper, ErrorMessageMapper errorMessageMapper, ObjectMapper objectMapper) {
 *             return new ApiErrorResponseAccessDeniedHandler(objectMapper, httpStatusMapper, errorCodeMapper, errorMessageMapper);
 *         }
 *
 *         &#64;Bean
 *         public SecurityFilterChain securityFilterChain(HttpSecurity http,
 *                                                        AccessDeniedHandler accessDeniedHandler) throws Exception {
 *             http.httpBasic().disable();
 *
 *             http.authorizeHttpRequests().anyRequest().authenticated();
 *
 *             http.exceptionHandling().accessDeniedHandler(accessDeniedHandler);
 *
 *             return http.build();
 *         }
 *     }
 * </pre>
 *
 * @see UnauthorizedEntryPoint
 */
public class ApiErrorResponseAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;
    private final HttpStatusMapper httpStatusMapper;
    private final ErrorCodeMapper errorCodeMapper;
    private final ErrorMessageMapper errorMessageMapper;

    public ApiErrorResponseAccessDeniedHandler(ObjectMapper objectMapper, HttpStatusMapper httpStatusMapper, ErrorCodeMapper errorCodeMapper,
                                               ErrorMessageMapper errorMessageMapper) {
        this.objectMapper = objectMapper;
        this.httpStatusMapper = httpStatusMapper;
        this.errorCodeMapper = errorCodeMapper;
        this.errorMessageMapper = errorMessageMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {
        ApiErrorResponse errorResponse = createResponse(accessDeniedException);

        response.setStatus(errorResponse.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    public ApiErrorResponse createResponse(AccessDeniedException exception) {
        HttpStatusCode httpStatus = httpStatusMapper.getHttpStatus(exception, HttpStatus.FORBIDDEN);
        String code = errorCodeMapper.getErrorCode(exception);
        String message = errorMessageMapper.getErrorMessage(exception);

        return new ApiErrorResponse(httpStatus, code, message);
    }

}
