package org.coffeeshop.purchaseorders.dtos;

/**
 * DTO for detailed order item data, including menu item info. Used for response payload when
 * fetching data to populate order cards in the frontend.
 *
 * @param purchaseOrderId the ID of the parent purchase order
 * @param menuItemTypeId the ID of the ordered menu item type
 * @param itemName the display name of the menu item
 * @param itemDescription a description of the menu item
 * @param size the size variant name
 * @param unitPrice the unit price at the time of order
 * @param quantity the number of units ordered
 * @param lineTotal the line total (unitPrice * quantity)
 */
public record OrderItemDetailsDto(
        Long purchaseOrderId,
        Long menuItemTypeId,
        String itemName,
        String itemDescription,
        String size,
        double unitPrice,
        int quantity,
        double lineTotal) {}
