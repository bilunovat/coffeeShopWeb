package org.coffeeshop.purchaseorders.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.coffeeshop.purchaseorders.models.MenuItemSize;

/**
 * Request DTO for creating a new menu item type (size variant). Represents a single size and price
 * combination within a menu item.
 *
 * @param size the size variant of the menu item type (required, must be a valid {@link
 *     MenuItemSize})
 * @param price the price for this size variant (must be positive)
 */
public record CreateMenuItemTypeDto(@NotNull MenuItemSize size, @Positive double price) {}
