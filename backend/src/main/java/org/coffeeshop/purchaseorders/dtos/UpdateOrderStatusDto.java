package org.coffeeshop.purchaseorders.dtos;

import jakarta.validation.constraints.NotNull;
import org.coffeeshop.purchaseorders.models.OrderStatus;

/**
 * Request DTO for updating the status of an existing purchase order.
 *
 * @param orderStatus the new status to set (must not be null)
 */
public record UpdateOrderStatusDto(@NotNull OrderStatus orderStatus) {}
