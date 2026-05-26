package org.coffeeshop.exceptions.userexceptions;

/** Exception thrown when a customer-related service operation fails. */
public class CustomerServiceException extends RuntimeException {
    /**
     * Constructs the exception with a descriptive message.
     *
     * @param message the detail message
     */
    public CustomerServiceException(String message) {
        super(message);
    }

    /**
     * Constructs the exception with a descriptive message and underlying cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public CustomerServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
