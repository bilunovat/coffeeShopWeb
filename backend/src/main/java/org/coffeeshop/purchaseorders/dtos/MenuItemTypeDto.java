package org.coffeeshop.purchaseorders.dtos;

import org.coffeeshop.purchaseorders.models.MenuItemSize;

/**
 * Response DTO for menu item type data. Contains size, price, and availability for a specific
 * variant of a menu item.
 *
 * @param menuItemTypeId the unique menu item type identifier
 * @param menuItemId the ID of the parent menu item
 * @param size the size variant
 * @param price the price for this size variant
 * @param isAvailable whether this size variant is available for ordering
 */
public record MenuItemTypeDto(
        Long menuItemTypeId,
        Long menuItemId,
        MenuItemSize size,
        double price,
        boolean isAvailable) {}
