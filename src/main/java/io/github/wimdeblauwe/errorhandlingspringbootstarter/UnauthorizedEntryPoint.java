package io.github.wimdeblauwe.errorhandlingspringbootstarter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorCodeMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.ErrorMessageMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.mapper.HttpStatusMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Use this {@link AuthenticationEntryPoint} implementation if you want to have a consistent response
 * with how this library works when the user is not authorized.
 * <p>
 * It is impossible for the library to provide auto-configuration for this. So you need to manually add
 * this to your security configuration. For example:
 *
 * <pre>
 *     public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
 *         &#64;Bean
 *         public UnauthorizedEntryPoint unauthorizedEntryPoint(HttpStatusMapper httpStatusMapper, ErrorCodeMapper errorCodeMapper, ErrorMessageMapper errorMessageMapper, ObjectMapper objectMapper) {
 *             return new UnauthorizedEntryPoint(httpStatusMapper, errorCodeMapper, errorMessageMapper, objectMapper);
 *         }
 *
 *         &#64;Bean
 *         public SecurityFilterChain securityFilterChain(HttpSecurity http,
 *                                                        UnauthorizedEntryPoint unauthorizedEntryPoint) throws Exception {
 *             http.httpBasic().disable();
 *
 *             http.authorizeHttpRequests().anyRequest().authenticated();
 *
 *             http.exceptionHandling().authenticationEntryPoint(unauthorizedEntryPoint);
 *
 *             return http.build();
 *         }
 *     }
 * </pre>
 */
public class UnauthorizedEntryPoint implements AuthenticationEntryPoint {

    protected final HttpStatusMapper httpStatusMapper;
    protected final ErrorCodeMapper errorCodeMapper;
    protected final ErrorMessageMapper errorMessageMapper;
    protected final ObjectMapper objectMapper;

    public UnauthorizedEntryPoint(HttpStatusMapper httpStatusMapper, ErrorCodeMapper errorCodeMapper, ErrorMessageMapper errorMessageMapper, ObjectMapper objectMapper) {
        this.httpStatusMapper = httpStatusMapper;
        this.errorCodeMapper = errorCodeMapper;
        this.errorMessageMapper = errorMessageMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws JsonProcessingException, IOException {
        ApiErrorResponse errorResponse = createResponse(authException);

        response.setStatus(errorResponse.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    public ApiErrorResponse createResponse(AuthenticationException exception) {
        HttpStatusCode httpStatus = httpStatusMapper.getHttpStatus(exception, HttpStatus.UNAUTHORIZED);
        String code = errorCodeMapper.getErrorCode(exception);
        String message = errorMessageMapper.getErrorMessage(exception);

        return new ApiErrorResponse(httpStatus, code, message);
    }

}
