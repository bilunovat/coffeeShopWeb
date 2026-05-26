package org.coffeeshop.purchaseorders.models;

import jakarta.persistence.*;

/**
 * JPA entity representing a specific size variant of a {@link MenuItem}. Each type has a size,
 * price, and availability flag. Linked to its parent menu item via a many-to-one relationship.
 */
@Entity
@Table(name = "menu_item_type")
public class MenuItemType {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_item_type_id")
    private Long menuItemTypeId;

    /** The parent menu item this type belongs to. */
    @ManyToOne
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    /** The size variant of this menu item type. */
    @Enumerated(EnumType.STRING)
    @Column(name = "size_name")
    private MenuItemSize size;

    /** The price for this size variant. */
    @Column(name = "price")
    private double price;

    /** Whether this size variant is currently available for ordering. */
    @Column(name = "is_available")
    private boolean isAvailable;

    /** No-arg constructor required by JPA. */
    protected MenuItemType() {}

    /**
     * Full constructor including the menu item type ID.
     *
     * @param menuItemTypeId the primary key (null for new entities)
     * @param menuItem the parent menu item
     * @param size the size variant
     * @param price the price
     * @param isAvailable the availability flag
     */
    public MenuItemType(
            Long menuItemTypeId,
            MenuItem menuItem,
            MenuItemSize size,
            double price,
            boolean isAvailable) {
        this.menuItemTypeId = menuItemTypeId;
        this.menuItem = menuItem;
        this.size = size;
        this.price = price;
        this.isAvailable = isAvailable;
    }

    /**
     * Constructor without ID for creating new entities.
     *
     * @param menuItem the parent menu item
     * @param size the size variant
     * @param price the price
     * @param isAvailable the availability flag
     */
    public MenuItemType(MenuItem menuItem, MenuItemSize size, double price, boolean isAvailable) {
        this(null, menuItem, size, price, isAvailable);
    }

    /** Returns the menu item type identifier. */
    public Long getMenuItemTypeId() {
        return menuItemTypeId;
    }

    /** Returns the parent menu item. */
    public MenuItem getMenuItem() {
        return menuItem;
    }

    /** Returns the size variant of this menu item type. */
    public MenuItemSize getSize() {
        return size;
    }

    /** Returns the price for this size variant. */
    public double getPrice() {
        return price;
    }

    /** Returns whether this size variant is currently available for ordering. */
    public boolean isAvailable() {
        return isAvailable;
    }
}
