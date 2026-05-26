package org.coffeeshop.purchaseorders.models;

/** Enum defining the lifecycle stages of a purchase order. */
public enum OrderStatus {
    /** Order has been accepted and is awaiting preparation. */
    ACCEPTED,
    /** Order is currently being prepared. */
    IN_PROGRESS,
    /** Order has been prepared and is ready for collection. */
    COMPLETED,
    /** Order has been collected by the customer. */
    COLLECTED,
    /** Order has been cancelled and cannot transition further. */
    CANCELLED
}
