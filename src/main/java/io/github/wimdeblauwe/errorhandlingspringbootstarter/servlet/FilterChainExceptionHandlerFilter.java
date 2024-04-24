package io.github.wimdeblauwe.errorhandlingspringbootstarter.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ApiErrorResponse;
import io.github.wimdeblauwe.errorhandlingspringbootstarter.ErrorHandlingFacade;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class FilterChainExceptionHandlerFilter extends OncePerRequestFilter {

    private final ErrorHandlingFacade errorHandlingFacade;
    private final ObjectMapper objectMapper;

    public FilterChainExceptionHandlerFilter(ErrorHandlingFacade errorHandlingFacade, ObjectMapper objectMapper) {
        this.errorHandlingFacade = errorHandlingFacade;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            ApiErrorResponse errorResponse = errorHandlingFacade.handle(ex);
            response.setStatus(errorResponse.getHttpStatus().value());
            var jsonResponseBody = objectMapper.writeValueAsString(errorResponse);
            response.getWriter().write(jsonResponseBody);
        }
    }
}
