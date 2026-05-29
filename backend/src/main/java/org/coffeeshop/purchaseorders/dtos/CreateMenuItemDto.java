package org.coffeeshop.purchaseorders.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request DTO for creating a new menu item. Contains the item details and a nested list of size and
 * price variants.
 *
 * @param name the display name of the menu item (required)
 * @param description a description of the menu item
 * @param isAvailable whether the menu item is currently available for ordering
 * @param menuItemTypes the list of size and price variants for this menu item (must not be empty)
 */
public record CreateMenuItemDto(
        @NotBlank String name,
        String description,
        boolean isAvailable,
        @NotEmpty List<@Valid CreateMenuItemTypeDto> menuItemTypes) {}
