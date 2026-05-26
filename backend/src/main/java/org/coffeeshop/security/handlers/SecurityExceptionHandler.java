package org.coffeeshop.security.handlers;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Global exception handler that translates Spring Security exceptions into JSON responses. */
@RestControllerAdvice
public class SecurityExceptionHandler {

    /**
     * Handles authentication exceptions by returning a 401 Unauthorized response.
     *
     * @param ex the authentication exception
     * @return a response entity with status 401 and an error message
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(
            AuthenticationException ex) {
        String message =
                ex.getMessage() == null || ex.getMessage().isBlank()
                        ? "Authentication required"
                        : ex.getMessage();
        if (ex instanceof BadCredentialsException && "Bad credentials".equalsIgnoreCase(message)) {
            message = "Invalid username or password";
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", message));
    }

    /**
     * Handles access denied exceptions by returning a 403 Forbidden response.
     *
     * @param ex the access denied exception
     * @return a response entity with status 403 and an error message
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(
            AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied"));
    }
}
