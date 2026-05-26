package org.coffeeshop.purchaseorders.repositories;

import org.coffeeshop.purchaseorders.models.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link org.coffeeshop.purchaseorders.models.MenuItem} entities.
 */
@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {}
