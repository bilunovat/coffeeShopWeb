package org.coffeeshop.purchaseorders.services;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import org.coffeeshop.purchaseorders.dtos.OrderItemDetailsDto;
import org.coffeeshop.purchaseorders.dtos.OrderItemDto;
import org.coffeeshop.purchaseorders.models.MenuItemType;
import org.coffeeshop.purchaseorders.models.OrderItem;
import org.coffeeshop.purchaseorders.models.OrderItemKey;
import org.coffeeshop.purchaseorders.models.PurchaseOrder;
import org.coffeeshop.purchaseorders.repositories.OrderItemRepository;
import org.springframework.stereotype.Service;

/**
 * Service layer for order item operations. Provides read-only access to order items by composite
 * key or as a full list.
 */
@Service
public class OrderItemService {
    private final OrderItemRepository repository;

    /**
     * Constructs the service with the given order item repository.
     *
     * @param repository the order item repository
     */
    public OrderItemService(OrderItemRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieves an order item by its composite key (purchase order ID + menu item type ID).
     *
     * @param purchaseOrderId the parent purchase order ID
     * @param menuItemId the associated menu item type ID
     * @return the order item as a DTO
     * @throws jakarta.persistence.EntityNotFoundException if no order item exists with the given
     *     composite key
     */
    public OrderItemDto getById(Long purchaseOrderId, Long menuItemId) {
        OrderItemKey key = new OrderItemKey(purchaseOrderId, menuItemId);
        OrderItem entity =
                repository
                        .findById(key)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "OrderItem not found with purchaseOrderId: "
                                                        + purchaseOrderId
                                                        + ", menuItemId: "
                                                        + menuItemId));
        return toDto(entity);
    }

    /**
     * Retrieves all order items for a given order, with menu item details.
     *
     * @param orderId the purchase order ID to look up items for
     * @return a list of order item details DTOs for the specified order
     */
    public List<OrderItemDetailsDto> findDetailsByOrderId(Long orderId) {
        List<OrderItem> items = repository.findById_PurchaseOrderId(orderId);
        return items.stream().map(this::toDetailsDto).collect(Collectors.toList());
    }

    /**
     * Converts an OrderItem entity to an OrderItemDetailsDto.
     *
     * @param entity the order item entity
     * @return the corresponding details DTO
     */
    private OrderItemDetailsDto toDetailsDto(OrderItem entity) {
        PurchaseOrder po = entity.getPurchaseOrder();
        MenuItemType mu = entity.getMenuItemType();
        String itemName =
                mu != null && mu.getMenuItem() != null ? mu.getMenuItem().getName() : null;
        String itemDescription =
                mu != null && mu.getMenuItem() != null ? mu.getMenuItem().getDescription() : null;
        String size = mu != null && mu.getSize() != null ? mu.getSize().name() : null;
        return new OrderItemDetailsDto(
                po != null ? po.getId() : 0L,
                mu != null ? mu.getMenuItemTypeId() : 0L,
                itemName,
                itemDescription,
                size,
                entity.getUnitPrice(),
                entity.getQuantity(),
                entity.getLineTotal());
    }

    /**
     * Retrieves all order items.
     *
     * @return a list of all order items as DTOs
     */
    public List<OrderItemDto> findAllOrderItems() {
        List<OrderItem> orderItems = repository.findAll();
        return orderItems.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Retrieves all order items for a given purchase order.
     *
     * @param orderId the purchase order ID to filter by
     * @return a list of matching order items as DTOs
     */
    public List<OrderItemDto> findByOrderId(Long orderId) {
        List<OrderItem> items = repository.findById_PurchaseOrderId(orderId);
        return items.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Converts an OrderItem entity to an OrderItemDto. Handles null purchase order or menu item
     * type references by defaulting to 0L.
     *
     * @param entity the order item entity
     * @return the corresponding DTO
     */
    private OrderItemDto toDto(OrderItem entity) {
        PurchaseOrder po = entity.getPurchaseOrder();
        MenuItemType mu = entity.getMenuItemType();

        return new OrderItemDto(
                po != null ? po.getId() : 0L,
                mu != null ? mu.getMenuItemTypeId() : 0L,
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getLineTotal());
    }
}
