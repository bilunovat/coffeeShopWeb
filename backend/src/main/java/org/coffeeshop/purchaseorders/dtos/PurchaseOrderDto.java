package org.coffeeshop.purchaseorders.dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import org.coffeeshop.purchaseorders.models.OrderStatus;

/**
 * Response DTO for purchase order data. Contains order details including ID, customer, station,
 * dates, status, and total.
 *
 * @param orderId the unique order identifier
 * @param customerId the ID of the customer who placed the order
 * @param stationId the ID of the station preparing the order
 * @param staffId the ID of the staff preparing the order
 * @param isArchived whether the order is archived
 * @param orderDate the date the order was placed
 * @param pickupTime the requested pickup time
 * @param orderStatus the current lifecycle status of the order
 * @param totalAmount the total monetary amount for the order
 */
public record PurchaseOrderDto(
        Long orderId,
        Long customerId,
        Long stationId,
        Long staffId,
        boolean isArchived,
        LocalDate orderDate,
        LocalTime pickupTime,
        OrderStatus orderStatus,
        double totalAmount) {}
