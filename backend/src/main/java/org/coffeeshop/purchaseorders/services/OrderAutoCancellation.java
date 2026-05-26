package org.coffeeshop.purchaseorders.services;

import java.time.LocalTime;
import java.util.List;
import org.coffeeshop.purchaseorders.models.OrderStatus;
import org.coffeeshop.purchaseorders.models.PurchaseOrder;
import org.coffeeshop.purchaseorders.repositories.PurchaseOrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled component that automatically cancels completed purchase orders which have not been
 * collected within 15 minutes of their requested pickup time.
 *
 * <p>Runs every 60 seconds via Spring's {@code @Scheduled} mechanism. Queries the database for
 * completed orders whose pickup time plus the 15-minute grace period has elapsed, and sets their
 * status to {@link OrderStatus#CANCELLED}.
 */
@Component
public class OrderAutoCancellation {
    private final PurchaseOrderRepository orderRepository;
    private final PurchaseOrderService service;

    /**
     * Constructs the scheduler with the required repository and service.
     *
     * @param orderRepository the repository for querying purchase orders
     * @param service the service used to update order statuses
     */
    public OrderAutoCancellation(
            PurchaseOrderRepository orderRepository, PurchaseOrderService service) {
        this.orderRepository = orderRepository;
        this.service = service;
    }

    /**
     * Cancels all completed orders that have exceeded the 15-minute collection window.
     *
     * <p>Calculates the cutoff as the current time minus 15 minutes, queries for completed orders
     * with a pickup time before that cutoff, and updates each to {@link OrderStatus#CANCELLED}.
     */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void cancelUncollectedOrders() {
        LocalTime expiryTime = LocalTime.now().minusMinutes(15);
        List<PurchaseOrder> expired = orderRepository.findUncollectedBefore(expiryTime);
        for (PurchaseOrder order : expired) {
            service.cancelOrder(order.getId());
        }
    }
}
