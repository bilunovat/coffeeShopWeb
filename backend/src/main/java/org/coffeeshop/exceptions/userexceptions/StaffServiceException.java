package org.coffeeshop.exceptions.userexceptions;

/** Exception thrown when a staff-related service operation fails. */
public class StaffServiceException extends RuntimeException {
    /**
     * Constructs the exception with a descriptive message.
     *
     * @param message the detail message
     */
    public StaffServiceException(String message) {
        super(message);
    }

    /**
     * Constructs the exception with a descriptive message and underlying cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public StaffServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
