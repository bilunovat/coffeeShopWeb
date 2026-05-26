package org.coffeeshop.purchaseorders.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.coffeeshop.purchaseorders.dtos.CreateMenuItemDto;
import org.coffeeshop.purchaseorders.dtos.CreateMenuItemTypeDto;
import org.coffeeshop.purchaseorders.dtos.MenuItemDto;
import org.coffeeshop.purchaseorders.dtos.MenuItemTypeDto;
import org.coffeeshop.purchaseorders.models.MenuItem;
import org.coffeeshop.purchaseorders.models.MenuItemType;
import org.coffeeshop.purchaseorders.repositories.MenuItemRepository;
import org.coffeeshop.purchaseorders.repositories.MenuItemTypeRepository;
import org.springframework.stereotype.Service;

/**
 * Service layer for menu item operations. Retrieves menu items with their associated types (sizes
 * and prices) nested.
 */
@Service
public class MenuItemService {
    private final MenuItemRepository itemRepository;
    private final MenuItemTypeRepository typeRepository;

    /**
     * Constructs the service with the given menu item repository.
     *
     * @param itemRepository the menu item repository
     * @param typeRepository the menu item type repository
     */
    public MenuItemService(
            MenuItemRepository itemRepository, MenuItemTypeRepository typeRepository) {
        this.itemRepository = itemRepository;
        this.typeRepository = typeRepository;
    }

    /**
     * Retrieves a menu item by its ID, including nested size and price variants.
     *
     * @param id the menu item ID
     * @return the menu item as a DTO
     * @throws jakarta.persistence.EntityNotFoundException if no menu item exists with the given ID
     */
    public MenuItemDto getById(Long id) {
        MenuItem entity =
                itemRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "MenuItem not found with id: " + id));
        return toDto(entity);
    }

    /**
     * Retrieves all menu items with their nested size and price variants.
     *
     * @return a list of all menu items as DTOs
     */
    public List<MenuItemDto> findAllMenuItemTypes() {
        List<MenuItem> menuItems = itemRepository.findAll();
        return menuItems.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Creates a new menu item along with its size and price variants. The parent menu item is
     * persisted first, then each nested type is linked to it via the many-to-one relationship
     * before being saved.
     *
     * @param itemDto the creation DTO containing the item details and its type variants
     * @return the created menu item as a response DTO, including nested types
     */
    public MenuItemDto createMenuItem(CreateMenuItemDto itemDto) {
        MenuItem menuItem = new MenuItem(itemDto.name(), itemDto.description(), true);
        MenuItem savedItem = itemRepository.save(menuItem);

        List<MenuItemTypeDto> typeDtos = new ArrayList<>();
        for (CreateMenuItemTypeDto typeDto : itemDto.menuItemTypes()) {
            MenuItemType type = new MenuItemType(savedItem, typeDto.size(), typeDto.price(), true);
            MenuItemType savedType = typeRepository.save(type);

            typeDtos.add(
                    new MenuItemTypeDto(
                            savedType.getMenuItemTypeId(),
                            savedItem.getId(),
                            savedType.getSize(),
                            savedType.getPrice(),
                            savedType.isAvailable()));
        }

        return new MenuItemDto(
                savedItem.getId(),
                savedItem.getName(),
                savedItem.getDescription(),
                savedItem.isAvailable(),
                typeDtos);
    }

    /**
     * Deletes a menu item and all its associated size and price variants. The operation is
     * transactional — if any part fails, the entire deletion is rolled back. Child types are
     * removed automatically via orphan removal.
     *
     * @param id the menu item ID to delete
     * @throws jakarta.persistence.EntityNotFoundException if no menu item exists with the given ID
     */
    @Transactional
    public void deleteMenuItem(Long id) {
        MenuItem entity =
                itemRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "MenuItem not found with id: " + id));
        itemRepository.delete(entity);
    }

    /**
     * Converts a MenuItem entity to a MenuItemDto, including nested type variants.
     *
     * @param entity the menu item entity
     * @return the corresponding DTO
     */
    private MenuItemDto toDto(MenuItem entity) {
        List<MenuItemTypeDto> types =
                entity.getMenuItemTypes() != null
                        ? entity.getMenuItemTypes().stream()
                                .map(this::toTypeDto)
                                .collect(Collectors.toList())
                        : List.of();

        return new MenuItemDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.isAvailable(),
                types);
    }

    /**
     * Converts a MenuItemType entity to a MenuItemTypeDto.
     *
     * @param entity the menu item type entity
     * @return the corresponding DTO
     */
    private MenuItemTypeDto toTypeDto(MenuItemType entity) {
        MenuItem menuItem = entity.getMenuItem();
        return new MenuItemTypeDto(
                entity.getMenuItemTypeId(),
                menuItem != null ? menuItem.getId() : 0L,
                entity.getSize(),
                entity.getPrice(),
                entity.isAvailable());
    }
}
