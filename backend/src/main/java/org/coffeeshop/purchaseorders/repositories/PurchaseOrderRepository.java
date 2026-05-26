package org.coffeeshop.purchaseorders.repositories;

import java.time.LocalTime;
import java.util.List;
import org.coffeeshop.purchaseorders.models.OrderStatus;
import org.coffeeshop.purchaseorders.models.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link org.coffeeshop.purchaseorders.models.PurchaseOrder}
 * entities.
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    /**
     * Finds all purchase orders with the given status.
     *
     * @param status the order status to filter by
     * @return a list of matching purchase orders
     */
    List<PurchaseOrder> findByOrderStatus(OrderStatus status);

    /**
     * Finds all purchase orders belonging to the given customer.
     *
     * @param id the customer ID to filter by
     * @return a list of matching purchase orders
     */
    List<PurchaseOrder> findByCustomerCustomerId(Long id);

    /**
     * Finds all purchase orders belonging to the given staff member.
     *
     * @param id the staff ID to filter by
     * @return a list of matching purchase orders
     */
    List<PurchaseOrder> findByStaffStaffId(Long id);

    /**
     * Finds all completed purchase orders whose requested pickup time has passed the given cutoff,
     * indicating that the 15-minute collection grace period has expired. Matches orders from
     * previous days, or from today with a pickup time before the cutoff.
     *
     * @param expiryTime the cutoff time; completed orders with pickupTime before this on today's
     *     date are considered expired
     * @return a list of uncollected purchase orders past the grace period
     */
    @Query(
            "SELECT o FROM PurchaseOrder o WHERE o.orderStatus = 'COMPLETED' "
                    + "AND (o.orderDate < CURRENT_DATE OR "
                    + "(o.orderDate = CURRENT_DATE AND o.pickupTime < :expiryTime))")
    List<PurchaseOrder> findUncollectedBefore(@Param("expiryTime") LocalTime expiryTime);
}
