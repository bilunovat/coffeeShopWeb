package org.coffeeshop.purchaseorderstests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import org.coffeeshop.purchaseorders.dtos.CreateMenuItemDto;
import org.coffeeshop.purchaseorders.dtos.CreateMenuItemTypeDto;
import org.coffeeshop.purchaseorders.models.MenuItem;
import org.coffeeshop.purchaseorders.models.MenuItemSize;
import org.coffeeshop.purchaseorders.models.MenuItemType;
import org.coffeeshop.purchaseorders.repositories.MenuItemRepository;
import org.coffeeshop.purchaseorders.repositories.MenuItemTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for MenuItemController. Uses Spring Boot's testing support with MockMvc to
 * perform integration tests on the menu item endpoints. Tests are grouped by operation
 * using @Nested classes and are transactional to avoid persisting test data. All tests run with
 * ADMIN role authentication via @WithMockUser.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "ADMIN")
class MenuItemControllerTests {
    private static final Long FAULTY_ID = 999L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private EntityManager entityManager;

    @Autowired private MenuItemRepository menuItemRepository;
    @Autowired private MenuItemTypeRepository menuItemTypeRepository;

    /**
     * Clears all seeded menu item and type data before each test to ensure a clean slate regardless
     * of whether MenuItemDataLoader is active.
     */
    @BeforeEach
    void clearSeededData() {
        menuItemTypeRepository.deleteAll();
        menuItemRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Tests for POST /api/v1/menu-item — menu item creation scenarios including happy path and
     * validation errors.
     */
    @Nested
    class CreateMenuItemTests {
        /**
         * Verifies that creating a menu item with valid data returns 201 Created with the correct
         * fields including menuItemId, name, description, isAvailable, and nested menuItemTypes.
         * Also asserts the item and its types are persisted.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createMenuItem_returnsCreatedMenuItem() throws Exception {
            CreateMenuItemDto request =
                    new CreateMenuItemDto(
                            "Batch Brew",
                            "Some description.",
                            true,
                            List.of(
                                    new CreateMenuItemTypeDto(MenuItemSize.REGULAR, 3.50),
                                    new CreateMenuItemTypeDto(MenuItemSize.LARGE, 4.00)));

            mockMvc.perform(
                            post("/api/v1/menu-item")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.menuItemId").isNumber())
                    .andExpect(jsonPath("$.name").value("Batch Brew"))
                    .andExpect(jsonPath("$.description").value("Some description."))
                    .andExpect(jsonPath("$.isAvailable").value(true))
                    .andExpect(jsonPath("$.menuItemTypes.length()").value(2))
                    .andExpect(jsonPath("$.menuItemTypes[0].size").value("REGULAR"))
                    .andExpect(jsonPath("$.menuItemTypes[0].price").value(3.50));

            assertEquals(1, menuItemRepository.count());
            assertEquals(2, menuItemTypeRepository.count());
        }

        /**
         * Verifies that creating a menu item with a null name returns 400 Bad Request, as
         * the @NotBlank constraint on CreateMenuItemDto.name rejects null values.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createMenuItem_returns400WhenNameNull() throws Exception {
            CreateMenuItemDto request =
                    new CreateMenuItemDto(
                            null,
                            "Some description.",
                            true,
                            List.of(
                                    new CreateMenuItemTypeDto(MenuItemSize.REGULAR, 3.50),
                                    new CreateMenuItemTypeDto(MenuItemSize.LARGE, 4.00)));

            mockMvc.perform(
                            post("/api/v1/menu-item")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, menuItemRepository.count());
            assertEquals(0, menuItemTypeRepository.count());
        }

        /**
         * Verifies that creating a menu item with a blank name returns 400 Bad Request, as
         * the @NotBlank constraint on CreateMenuItemDto.name rejects whitespace-only values.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createMenuItem_returns400WhenNameEmpty() throws Exception {
            CreateMenuItemDto request =
                    new CreateMenuItemDto(
                            " ",
                            "Some description.",
                            true,
                            List.of(
                                    new CreateMenuItemTypeDto(MenuItemSize.REGULAR, 3.50),
                                    new CreateMenuItemTypeDto(MenuItemSize.LARGE, 4.00)));

            mockMvc.perform(
                            post("/api/v1/menu-item")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, menuItemRepository.count());
            assertEquals(0, menuItemTypeRepository.count());
        }

        /**
         * Verifies that creating a menu item with an empty menuItemTypes list returns 400 Bad
         * Request, as the @NotEmpty constraint on CreateMenuItemDto.menuItemTypes rejects empty
         * lists.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createMenuItem_returns400WhenListEmpty() throws Exception {
            CreateMenuItemDto request =
                    new CreateMenuItemDto("Batch Brew", "Some description.", true, List.of());

            mockMvc.perform(
                            post("/api/v1/menu-item")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, menuItemRepository.count());
            assertEquals(0, menuItemTypeRepository.count());
        }

        /**
         * Verifies that creating a menu item type with a null size returns 400 Bad Request, as
         * the @NotNull constraint on CreateMenuItemTypeDto.size rejects null values.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createMenuItem_returns400WhenItemTypeMissing() throws Exception {
            CreateMenuItemDto request =
                    new CreateMenuItemDto(
                            "Batch Brew",
                            "Some description.",
                            true,
                            List.of(
                                    new CreateMenuItemTypeDto(null, 3.50),
                                    new CreateMenuItemTypeDto(MenuItemSize.LARGE, 4.00)));

            mockMvc.perform(
                            post("/api/v1/menu-item")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, menuItemRepository.count());
            assertEquals(0, menuItemTypeRepository.count());
        }

        /**
         * Verifies that creating a menu item type with a negative price returns 400 Bad Request, as
         * the @Positive constraint on CreateMenuItemTypeDto.price rejects negative values.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createMenuItem_returns400WhenItemTypePriceNegative() throws Exception {
            CreateMenuItemDto request =
                    new CreateMenuItemDto(
                            "Batch Brew",
                            "Some description.",
                            true,
                            List.of(
                                    new CreateMenuItemTypeDto(MenuItemSize.REGULAR, -3.50),
                                    new CreateMenuItemTypeDto(MenuItemSize.LARGE, 4.00)));

            mockMvc.perform(
                            post("/api/v1/menu-item")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, menuItemRepository.count());
            assertEquals(0, menuItemTypeRepository.count());
        }

        /**
         * Verifies that creating a menu item type with a zero price returns 400 Bad Request, as
         * the @Positive constraint on CreateMenuItemTypeDto.price rejects zero (positive means
         * strictly greater than zero).
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createMenuItem_returns400WhenItemTypePriceZero() throws Exception {
            CreateMenuItemDto request =
                    new CreateMenuItemDto(
                            "Batch Brew",
                            "Some description.",
                            true,
                            List.of(
                                    new CreateMenuItemTypeDto(MenuItemSize.REGULAR, 0.00),
                                    new CreateMenuItemTypeDto(MenuItemSize.LARGE, 4.00)));

            mockMvc.perform(
                            post("/api/v1/menu-item")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, menuItemRepository.count());
            assertEquals(0, menuItemTypeRepository.count());
        }
    }

    /**
     * Tests for GET /api/v1/menu-item — menu item retrieval scenarios including get by ID, get all,
     * and 404 for missing items.
     */
    @Nested
    class GetMenuItemsTests {
        /**
         * Verifies that retrieving an existing menu item by ID returns 200 OK with the correct
         * fields including nested menuItemTypes with sizes and prices.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getMenuItemById_returnsMenuItem() throws Exception {
            MenuItem batch = saveTestMenuItem();

            mockMvc.perform(get("/api/v1/menu-item/{id}", batch.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.menuItemId").value(batch.getId()))
                    .andExpect(jsonPath("$.name").value("Batch Brew"))
                    .andExpect(jsonPath("$.description").value("Some description."))
                    .andExpect(jsonPath("$.isAvailable").value(true))
                    .andExpect(jsonPath("$.menuItemTypes.length()").value(2))
                    .andExpect(jsonPath("$.menuItemTypes[0].size").value("REGULAR"))
                    .andExpect(jsonPath("$.menuItemTypes[0].price").value(3.0))
                    .andExpect(jsonPath("$.menuItemTypes[1].size").value("LARGE"))
                    .andExpect(jsonPath("$.menuItemTypes[1].price").value(4.0));
        }

        /**
         * Verifies that GET /api/v1/menu-item returns 200 OK with a list containing all previously
         * created menu items and their nested types.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getMenuItems_returnsListOfAllMenuItems() throws Exception {
            MenuItem espresso =
                    menuItemRepository.save(new MenuItem("Espresso", "Some description.", true));
            menuItemTypeRepository.save(
                    new MenuItemType(espresso, MenuItemSize.REGULAR, 1.50, true));
            saveTestMenuItem();

            mockMvc.perform(get("/api/v1/menu-item"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(
                            jsonPath("$[?(@.name == 'Batch Brew')].description")
                                    .value("Some description."))
                    .andExpect(
                            jsonPath("$[?(@.name == 'Batch Brew')].menuItemTypes.length()")
                                    .value(2))
                    .andExpect(
                            jsonPath("$[?(@.name == 'Espresso')].menuItemTypes.length()").value(1));
        }

        /**
         * Verifies that retrieving a non-existent menu item ID returns 404 Not Found.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getMenuItemById_returns404ForMissingItem() throws Exception {
            mockMvc.perform(get("/api/v1/menu-item/{id}", FAULTY_ID))
                    .andExpect(status().isNotFound());
        }

        /**
         * Verifies that GET /api/v1/menu-item returns 200 OK with an empty list when no menu items
         * exist.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getMenuItem_returnsEmptyListWhenNoItems() throws Exception {
            mockMvc.perform(get("/api/v1/menu-item"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    /**
     * Tests for DELETE /api/v1/menu-item/{id} — menu item deletion scenarios including happy path
     * with orphan removal and 404 for missing items.
     */
    @Nested
    class DeleteMenuItemsTests {
        /**
         * Verifies that deleting an existing menu item returns 204 No Content, and that a
         * subsequent GET returns 404. Also asserts that associated MenuItemTypes are removed via
         * orphan removal.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void deleteMenuItem_returns204() throws Exception {
            MenuItem batch = saveTestMenuItem();

            mockMvc.perform(delete("/api/v1/menu-item/{id}", batch.getId()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/v1/menu-item/{id}", batch.getId()))
                    .andExpect(status().isNotFound());

            assertEquals(0, menuItemTypeRepository.count());
        }

        /**
         * Verifies that deleting a non-existent menu item returns 404 Not Found.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void deleteMenuItem_returns404ForMissingItem() throws Exception {
            mockMvc.perform(delete("/api/v1/menu-item/{id}", FAULTY_ID))
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * Creates and persists a test MenuItem ("Batch Brew") with two MenuItemTypes (REGULAR and
     * LARGE), then flushes and clears the persistence context to ensure fresh loading of
     * the @OneToMany relationship.
     *
     * @return the saved MenuItem entity
     */
    private MenuItem saveTestMenuItem() {
        MenuItem batch =
                menuItemRepository.save(new MenuItem("Batch Brew", "Some description.", true));
        menuItemTypeRepository.save(new MenuItemType(batch, MenuItemSize.REGULAR, 3.0, true));
        menuItemTypeRepository.save(new MenuItemType(batch, MenuItemSize.LARGE, 4.0, true));

        entityManager.flush();
        entityManager.clear();

        return batch;
    }
}
