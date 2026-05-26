package org.coffeeshop.purchaseorders.controllers;

import jakarta.validation.Valid;
import java.util.List;
import org.coffeeshop.purchaseorders.dtos.CreateMenuItemDto;
import org.coffeeshop.purchaseorders.dtos.MenuItemDto;
import org.coffeeshop.purchaseorders.services.MenuItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for browsing menu items. Provides endpoints for retrieving all menu items and
 * individual items by ID, with nested size and price information.
 */
@RestController
@RequestMapping("/api/v1/menu-item")
public class MenuItemController {
    private final MenuItemService service;

    /**
     * Constructs the controller with the given menu item service.
     *
     * @param service the menu item service handling business logic
     */
    public MenuItemController(MenuItemService service) {
        this.service = service;
    }

    /**
     * Retrieves all menu items with their nested size and price variants.
     *
     * @return 200 OK with a list of all menu item DTOs
     */
    @GetMapping
    public ResponseEntity<List<MenuItemDto>> findAll() {
        List<MenuItemDto> items = service.findAllMenuItemTypes();

        return ResponseEntity.ok(items);
    }

    /**
     * Retrieves a single menu item by its ID, including nested size and price variants.
     *
     * @param id the path variable specifying the menu item ID
     * @return 200 OK with the menu item DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<MenuItemDto> getById(@PathVariable("id") Long id) {
        MenuItemDto dto = service.getById(id);

        return ResponseEntity.ok(dto);
    }

    /**
     * Creates a new menu item with its nested size and price variants.
     *
     * @param itemDto the request body containing the menu item details and type variants
     * @return 201 Created with the newly created menu item DTO
     */
    @PostMapping
    public ResponseEntity<MenuItemDto> createMenuItem(
            @Valid @RequestBody CreateMenuItemDto itemDto) {
        MenuItemDto created = service.createMenuItem(itemDto);

        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Deletes a menu item and all its associated size and price variants by ID.
     *
     * @param id the path variable specifying the menu item ID to delete
     * @return 204 No Content on successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable("id") Long id) {
        service.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }
}
