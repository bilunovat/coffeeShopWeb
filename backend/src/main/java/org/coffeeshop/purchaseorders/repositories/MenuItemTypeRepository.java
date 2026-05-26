package org.coffeeshop.purchaseorders.repositories;

import org.coffeeshop.purchaseorders.models.MenuItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link org.coffeeshop.purchaseorders.models.MenuItemType}
 * entities.
 */
@Repository
public interface MenuItemTypeRepository extends JpaRepository<MenuItemType, Long> {}
