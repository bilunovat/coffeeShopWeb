package org.coffeeshop.purchaseorders.dtos;

/**
 * Response DTO for order item data. Contains the parent order ID, menu item type ID, quantity, unit
 * price, and line total.
 *
 * @param purchaseOrderId the ID of the parent purchase order
 * @param menuItemTypeId the ID of the ordered menu item type
 * @param quantity the number of units ordered
 * @param unitPrice the unit price at the time of order
 * @param lineTotal the line total (unitPrice * quantity)
 */
public record OrderItemDto(
        Long purchaseOrderId,
        Long menuItemTypeId,
        int quantity,
        double unitPrice,
        double lineTotal) {}
