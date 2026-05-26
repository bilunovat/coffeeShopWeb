package org.coffeeshop.purchaseorders.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.List;

/**
 * Request DTO for creating a new purchase order. Contains customer and station references, pickup
 * time, and a list of order items.
 *
 * @param customerId the ID of the customer placing the order (must not be null)
 * @param stationId the ID of the station preparing the order (must not be null)
 * @param pickupTime the requested pickup time (must not be null)
 * @param orderItems the list of order items (must not be empty, each item validated)
 */
public record CreatePurchaseOrderDto(
        @NotNull Long customerId,
        @NotNull Long stationId,
        @NotNull LocalTime pickupTime,
        @NotEmpty List<@Valid CreateOrderItemDto> orderItems) {}
