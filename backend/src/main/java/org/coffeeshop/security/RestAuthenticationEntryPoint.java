package org.coffeeshop.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/** Returns a JSON 401 response for unauthenticated REST API requests. */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    /**
     * Sends a JSON-formatted 401 Unauthorized response.
     *
     * @param request the incoming HTTP request
     * @param response the outgoing HTTP response
     * @param authException the authentication exception that triggered this entry point
     * @throws IOException if writing the response fails
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"" + resolveMessage(authException) + "\"}");
    }

    private String resolveMessage(AuthenticationException authException) {
        if (authException == null
                || authException.getMessage() == null
                || authException.getMessage().isBlank()) {
            return "Authentication required";
        }
        if (authException instanceof BadCredentialsException
                && "Bad credentials".equalsIgnoreCase(authException.getMessage())) {
            return "Invalid username or password";
        }
        return authException.getMessage();
    }
}
