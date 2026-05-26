package org.coffeeshop.purchaseorders.repositories;

import java.util.List;
import org.coffeeshop.purchaseorders.models.OrderItem;
import org.coffeeshop.purchaseorders.models.OrderItemKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link org.coffeeshop.purchaseorders.models.OrderItem} entities.
 * Uses composite key {@link org.coffeeshop.purchaseorders.models.OrderItemKey}.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemKey> {
    /** Finds all order items belonging to the given purchase order. */
    List<OrderItem> findById_PurchaseOrderId(Long purchaseOrderId);
}
