package org.coffeeshop.exceptions.purchaseorderexceptions;

/**
 * Exception thrown when an invalid order status transition is attempted, such as changing the
 * status of an already cancelled order.
 */
public class InvalidOrderStatusTransition extends RuntimeException {
    /**
     * Constructs the exception with a descriptive message.
     *
     * @param message the detail message explaining why the transition is invalid
     */
    public InvalidOrderStatusTransition(String message) {
        super(message);
    }
}
