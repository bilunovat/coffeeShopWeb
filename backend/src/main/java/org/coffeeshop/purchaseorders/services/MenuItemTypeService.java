package org.coffeeshop.purchaseorders.services;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import org.coffeeshop.purchaseorders.dtos.MenuItemTypeDto;
import org.coffeeshop.purchaseorders.models.MenuItem;
import org.coffeeshop.purchaseorders.models.MenuItemType;
import org.coffeeshop.purchaseorders.repositories.MenuItemTypeRepository;
import org.springframework.stereotype.Service;

/**
 * Service layer for menu item type operations. Provides read-only access to item types (size
 * variants) by ID or as a full list.
 */
@Service
public class MenuItemTypeService {
    private final MenuItemTypeRepository repository;

    /**
     * Constructs the service with the given menu item type repository.
     *
     * @param repository the menu item type repository
     */
    public MenuItemTypeService(MenuItemTypeRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieves a menu item type by its ID.
     *
     * @param id the menu item type ID
     * @return the menu item type as a DTO
     * @throws jakarta.persistence.EntityNotFoundException if no menu item type exists with the
     *     given ID
     */
    public MenuItemTypeDto getById(Long id) {
        MenuItemType entity =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "MenuItemType not found with id: " + id));
        return toDto(entity);
    }

    /**
     * Retrieves all menu item types.
     *
     * @return a list of all menu item types as DTOs
     */
    public List<MenuItemTypeDto> findAllMenuItemTypes() {
        List<MenuItemType> menuItemTypes = repository.findAll();
        return menuItemTypes.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Converts a MenuItemType entity to a MenuItemTypeDto.
     *
     * @param entity the menu item type entity
     * @return the corresponding DTO
     */
    private MenuItemTypeDto toDto(MenuItemType entity) {
        MenuItem menuItem = entity.getMenuItem();

        return new MenuItemTypeDto(
                entity.getMenuItemTypeId(),
                menuItem != null ? menuItem.getId() : 0L,
                entity.getSize(),
                entity.getPrice(),
                entity.isAvailable());
    }
}
