package org.coffeeshop.purchaseorderstests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.coffeeshop.purchaseorders.dtos.CreateOrderItemDto;
import org.coffeeshop.purchaseorders.dtos.CreatePurchaseOrderDto;
import org.coffeeshop.purchaseorders.dtos.UpdateOrderStatusDto;
import org.coffeeshop.purchaseorders.models.MenuItem;
import org.coffeeshop.purchaseorders.models.MenuItemSize;
import org.coffeeshop.purchaseorders.models.MenuItemType;
import org.coffeeshop.purchaseorders.models.OrderStatus;
import org.coffeeshop.purchaseorders.models.PurchaseOrder;
import org.coffeeshop.purchaseorders.repositories.MenuItemRepository;
import org.coffeeshop.purchaseorders.repositories.MenuItemTypeRepository;
import org.coffeeshop.purchaseorders.repositories.PurchaseOrderRepository;
import org.coffeeshop.purchaseorders.services.OrderAutoCancellation;
import org.coffeeshop.stations.models.Station;
import org.coffeeshop.stations.repositories.StationRepository;
import org.coffeeshop.users.models.Customer;
import org.coffeeshop.users.models.Staff;
import org.coffeeshop.users.repositories.CustomerRepository;
import org.coffeeshop.users.repositories.StaffRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for PurchaseOrderController. Uses Spring Boot's testing support with MockMvc to
 * perform integration tests on the purchase order endpoints. Tests are grouped by operation
 * using @Nested classes and are transactional to avoid persisting test data. All tests run with
 * ADMIN role authentication via @WithMockUser.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "ADMIN")
class PurchaseOrderControllerTests {
    private static final Long FAULTY_ID = 999L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private CustomerRepository customerRepository;
    @Autowired private StationRepository stationRepository;
    @Autowired private StaffRepository staffRepository;
    @Autowired private PurchaseOrderRepository orderRepository;
    @Autowired private MenuItemRepository menuItemRepository;
    @Autowired private MenuItemTypeRepository menuItemTypeRepository;

    @Autowired private OrderAutoCancellation orderAutoCancellation;

    /**
     * Tests for POST /api/v1/order — order creation scenarios including happy path, validation
     * errors, missing entities, and edge cases.
     */
    @Nested
    class CreateOrderTests {
        /**
         * Verifies that creating an order with valid data returns 201 with the correct order fields
         * including orderId, customerId, stationId, orderStatus (ACCEPTED), and the calculated
         * totalAmount. Also asserts the order is persisted in the repository.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createOrder_returnsCreatedOrder() throws Exception {
            Customer customer = saveTestCustomer();
            Station station = saveTestStation();
            MenuItemType menuItemType = saveTestMenuItemType();

            int quantity = 2;
            CreatePurchaseOrderDto request =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            station.getId(),
                            LocalTime.of(23, 57),
                            List.of(
                                    new CreateOrderItemDto(
                                            menuItemType.getMenuItemTypeId(), quantity)));

            double expectedValue = menuItemType.getPrice() * quantity;
            mockMvc.perform(
                            post("/api/v1/order")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.orderId").isNumber())
                    .andExpect(jsonPath("$.customerId").value(customer.getId()))
                    .andExpect(jsonPath("$.stationId").value(station.getId()))
                    .andExpect(jsonPath("$.orderStatus").value("ACCEPTED"))
                    .andExpect(jsonPath("$.totalAmount").value(expectedValue));

            assertEquals(1, orderRepository.count());
        }

        /**
         * Verifies that creating an order with quantity 0 returns 400 Bad Request, as the @Positive
         * constraint on CreateOrderItemDto.quantity rejects non-positive values.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createOrder_returns400WhenQuantityInvalid() throws Exception {
            Customer customer = saveTestCustomer();
            Station station = saveTestStation();
            MenuItemType menuItemType = saveTestMenuItemType();

            CreatePurchaseOrderDto request =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            station.getId(),
                            LocalTime.of(23, 58),
                            List.of(new CreateOrderItemDto(menuItemType.getMenuItemTypeId(), 0)));

            mockMvc.perform(
                            post("/api/v1/order")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, orderRepository.count());
        }

        /**
         * Verifies that creating an order with a non-existent menuItemTypeId returns 404 Not Found,
         * as the service throws EntityNotFoundException when the menu item type cannot be found.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createOrder_returns404WhenMenuItemTypeMissing() throws Exception {
            Customer customer = saveTestCustomer();
            Station station = saveTestStation();

            CreatePurchaseOrderDto request =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            station.getId(),
                            LocalTime.of(23, 58),
                            List.of(new CreateOrderItemDto(FAULTY_ID, 1)));

            mockMvc.perform(
                            post("/api/v1/order")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            assertEquals(0, orderRepository.count());
        }

        /**
         * Verifies that creating an order with a non-existent customerId returns 404 Not Found, as
         * the service throws EntityNotFoundException when the customer cannot be found.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createOrder_returns404WhenCustomerMissing() throws Exception {
            Station station = saveTestStation();
            MenuItemType menuItemType = saveTestMenuItemType();

            CreatePurchaseOrderDto request =
                    new CreatePurchaseOrderDto(
                            FAULTY_ID,
                            station.getId(),
                            LocalTime.of(23, 58),
                            List.of(new CreateOrderItemDto(menuItemType.getMenuItemTypeId(), 1)));

            mockMvc.perform(
                            post("/api/v1/order")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            assertEquals(0, orderRepository.count());
        }

        /**
         * Verifies that creating an order with a null customerId returns 400 Bad Request, as
         * the @NotNull constraint on CreatePurchaseOrderDto.customerId rejects null values.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createOrder_returns400WhenCustomerNull() throws Exception {
            Station station = saveTestStation();
            MenuItemType menuItemType = saveTestMenuItemType();

            CreatePurchaseOrderDto request =
                    new CreatePurchaseOrderDto(
                            null,
                            station.getId(),
                            LocalTime.of(23, 58),
                            List.of(new CreateOrderItemDto(menuItemType.getMenuItemTypeId(), 1)));

            mockMvc.perform(
                            post("/api/v1/order")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, orderRepository.count());
        }

        /**
         * Verifies that creating an order with a non-existent stationId returns 404 Not Found, as
         * the service throws EntityNotFoundException when the station cannot be found.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createOrder_returns404WhenStationMissing() throws Exception {
            Customer customer = saveTestCustomer();
            MenuItemType menuItemType = saveTestMenuItemType();

            CreatePurchaseOrderDto request =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            FAULTY_ID,
                            LocalTime.of(23, 58),
                            List.of(new CreateOrderItemDto(menuItemType.getMenuItemTypeId(), 1)));

            mockMvc.perform(
                            post("/api/v1/order")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            assertEquals(0, orderRepository.count());
        }

        /**
         * Verifies that creating an order with an empty orderItems list returns 400 Bad Request, as
         * the @NotEmpty constraint on CreatePurchaseOrderDto.orderItems rejects empty lists.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createOrder_returns400WhenItemsListEmpty() throws Exception {
            Customer customer = saveTestCustomer();
            Station station = saveTestStation();

            CreatePurchaseOrderDto request =
                    new CreatePurchaseOrderDto(
                            customer.getId(), station.getId(), LocalTime.of(23, 58), List.of());

            mockMvc.perform(
                            post("/api/v1/order")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, orderRepository.count());
        }

        /**
         * Verifies that creating an order with a pickup time outside the station's opening hours
         * returns 400 Bad Request, as the service validates the pickup time against the station
         * schedule.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createOrder_returns400WhenStationClosed() throws Exception {
            Customer customer = saveTestCustomer();
            Station station = saveTestStation();
            MenuItemType menuItemType = saveTestMenuItemType();

            CreatePurchaseOrderDto request =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            station.getId(),
                            LocalTime.of(23, 59),
                            List.of(new CreateOrderItemDto(menuItemType.getMenuItemTypeId(), 1)));

            mockMvc.perform(
                            post("/api/v1/order")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, orderRepository.count());
        }

        /**
         * Verifies that creating an order with multiple order items returns 201 Created and that
         * the totalAmount is correctly calculated as the sum of (unitPrice * quantity) for each
         * item.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createOrder_returns201ForMultipleItems() throws Exception {
            Customer customer = saveTestCustomer();
            Station station = saveTestStation();

            MenuItem latte =
                    menuItemRepository.save(
                            new MenuItem("Latte", "Espresso with steamed milk", true));
            MenuItemType regLatte =
                    menuItemTypeRepository.save(
                            new MenuItemType(latte, MenuItemSize.REGULAR, 2.50, true));

            MenuItem americano =
                    menuItemRepository.save(
                            new MenuItem("Americano", "Espresso with boiled water", true));
            MenuItemType regAmericano =
                    menuItemTypeRepository.save(
                            new MenuItemType(americano, MenuItemSize.REGULAR, 1.50, true));

            int latteQuantity = 1;
            int americanoQuantity = 2;
            CreatePurchaseOrderDto request =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            station.getId(),
                            LocalTime.of(23, 58),
                            List.of(
                                    new CreateOrderItemDto(
                                            regLatte.getMenuItemTypeId(), latteQuantity),
                                    new CreateOrderItemDto(
                                            regAmericano.getMenuItemTypeId(), americanoQuantity)));

            double expectedValue =
                    regLatte.getPrice() * latteQuantity
                            + regAmericano.getPrice() * americanoQuantity;
            mockMvc.perform(
                            post("/api/v1/order")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.totalAmount").value(expectedValue));

            assertEquals(1, orderRepository.count());
        }
    }

    /**
     * Tests for GET /api/v1/order — order retrieval scenarios including get by ID, get all, filter
     * by status, and filter by customer.
     */
    @Nested
    class GetOrderTests {
        /**
         * Verifies that retrieving an existing order by ID returns 200 OK with the correct orderId,
         * customerId, stationId, and orderStatus.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getOrderById_returnsOrder() throws Exception {
            Customer customer = saveTestCustomer();
            Station station = saveTestStation();
            MenuItemType menuItemType = saveTestMenuItemType();

            CreatePurchaseOrderDto createRequest =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            station.getId(),
                            LocalTime.of(23, 57),
                            List.of(new CreateOrderItemDto(menuItemType.getMenuItemTypeId(), 2)));

            String responseJson =
                    mockMvc.perform(
                                    post("/api/v1/order")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    objectMapper.writeValueAsString(createRequest)))
                            .andExpect(status().isCreated())
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

            Long orderId = objectMapper.readTree(responseJson).get("orderId").asLong();

            mockMvc.perform(get("/api/v1/order/{id}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(orderId))
                    .andExpect(jsonPath("$.customerId").value(customer.getId()))
                    .andExpect(jsonPath("$.stationId").value(station.getId()))
                    .andExpect(jsonPath("$.orderStatus").value("ACCEPTED"));
        }

        /**
         * Verifies that retrieving a non-existent order ID returns 404 Not Found.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getOrderById_returns404ForMissingOrder() throws Exception {
            mockMvc.perform(get("/api/v1/order/{id}", FAULTY_ID)).andExpect(status().isNotFound());
        }

        /**
         * Verifies that GET /api/v1/order (no query params) returns 200 OK with a list containing
         * all previously created orders.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getOrder_returnsListOfAllOrders() throws Exception {
            Customer customer = saveTestCustomer();
            Station station = saveTestStation();
            MenuItemType menuItemType = saveTestMenuItemType();

            CreatePurchaseOrderDto orderOne =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            station.getId(),
                            LocalTime.of(23, 57),
                            List.of(new CreateOrderItemDto(menuItemType.getMenuItemTypeId(), 2)));
            CreatePurchaseOrderDto orderTwo =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            station.getId(),
                            LocalTime.of(23, 57),
                            List.of(new CreateOrderItemDto(menuItemType.getMenuItemTypeId(), 1)));

            mockMvc.perform(
                    post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderOne)));

            mockMvc.perform(
                    post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderTwo)));

            mockMvc.perform(get("/api/v1/order"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        /**
         * Verifies that filtering orders by a status with no matching orders returns 200 OK with an
         * empty list.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getOrdersByStatus_returnsEmptyListWhenNoMatch() throws Exception {
            Customer customer = saveTestCustomer();
            Station station = saveTestStation();
            MenuItemType menuItemType = saveTestMenuItemType();

            CreatePurchaseOrderDto request =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            station.getId(),
                            LocalTime.of(23, 57),
                            List.of(new CreateOrderItemDto(menuItemType.getMenuItemTypeId(), 2)));

            mockMvc.perform(
                    post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));

            mockMvc.perform(get("/api/v1/order/status/IN_PROGRESS"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        /**
         * Verifies that filtering orders by status returns 200 OK with a list containing only
         * orders matching the given status.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getOrdersByStatus_returnsFilteredList() throws Exception {
            Customer customer = saveTestCustomer();
            Station station = saveTestStation();
            MenuItemType menuItemType = saveTestMenuItemType();

            CreatePurchaseOrderDto orderOne =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            station.getId(),
                            LocalTime.of(23, 57),
                            List.of(new CreateOrderItemDto(menuItemType.getMenuItemTypeId(), 2)));
            CreatePurchaseOrderDto orderTwo =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            station.getId(),
                            LocalTime.of(23, 57),
                            List.of(new CreateOrderItemDto(menuItemType.getMenuItemTypeId(), 1)));

            mockMvc.perform(
                    post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderOne)));

            mockMvc.perform(
                    post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderTwo)));

            mockMvc.perform(get("/api/v1/order/status/ACCEPTED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        /**
         * Verifies that filtering orders by customerId returns 200 OK with a list containing only
         * orders belonging to the specified customer.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getOrdersByCustomer_returnsFilteredList() throws Exception {
            Customer customer = saveTestCustomer();
            Station station = saveTestStation();
            MenuItemType menuItemType = saveTestMenuItemType();

            CreatePurchaseOrderDto orderOne =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            station.getId(),
                            LocalTime.of(23, 57),
                            List.of(new CreateOrderItemDto(menuItemType.getMenuItemTypeId(), 2)));
            CreatePurchaseOrderDto orderTwo =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            station.getId(),
                            LocalTime.of(23, 57),
                            List.of(new CreateOrderItemDto(menuItemType.getMenuItemTypeId(), 1)));

            mockMvc.perform(
                    post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderOne)));

            mockMvc.perform(
                    post("/api/v1/order")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderTwo)));

            Long customerId = customer.getId();
            mockMvc.perform(get("/api/v1/order?customerId={customerId}", customerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        /**
         * Verifies that filtering orders by a non-existent customerId returns 200 OK with an empty
         * list.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getOrdersByCustomer_returnsEmptyListForMissingCustomer() throws Exception {
            mockMvc.perform(get("/api/v1/order?customerId={customerId}", FAULTY_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        /**
         * Verifies that filtering orders by staffId returns 200 OK with a list containing only
         * orders belonging to the specified staff.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getOrdersByStaff_returnsFilteredList() throws Exception {
            Customer customer = saveTestCustomer();
            Station station = saveTestStation();
            MenuItemType menuItemType = saveTestMenuItemType();
            Staff staff = saveTestStaff();

            CreatePurchaseOrderDto createRequest =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            station.getId(),
                            LocalTime.of(23, 57),
                            List.of(new CreateOrderItemDto(menuItemType.getMenuItemTypeId(), 2)));

            String responseJson =
                    mockMvc.perform(
                                    post("/api/v1/order")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    objectMapper.writeValueAsString(createRequest)))
                            .andExpect(status().isCreated())
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

            Long orderId = objectMapper.readTree(responseJson).get("orderId").asLong();

            mockMvc.perform(
                            put("/api/v1/order/{orderId}/{staffId}/status", orderId, staff.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            objectMapper.writeValueAsString(
                                                    new UpdateOrderStatusDto(
                                                            OrderStatus.IN_PROGRESS))))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/order?staffId={staffId}", staff.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].staffId").value(staff.getId()));
        }

        /**
         * Verifies that filtering orders by a non-existent staffId returns 200 OK with an empty
         * list.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getOrdersByStaffId_returnsEmptyListWhenNoMatch() throws Exception {
            Staff staff = saveTestStaff();

            mockMvc.perform(get("/api/v1/order?staffId={staffId}", staff.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    /**
     * Tests for PUT /api/v1/order/{id}/status — order status update scenarios including happy path,
     * missing order, null status, and invalid transitions.
     */
    @Nested
    class UpdateOrderStatusTests {
        /**
         * Verifies that updating an existing order's status to IN_PROGRESS returns 200 OK, and that
         * a subsequent GET confirms the status has been persisted.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateOrderStatus_returns200() throws Exception {
            Customer customer = saveTestCustomer();
            Station station = saveTestStation();
            Staff staff = saveTestStaff();
            MenuItemType menuItemType = saveTestMenuItemType();

            CreatePurchaseOrderDto createRequest =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            station.getId(),
                            LocalTime.of(23, 57),
                            List.of(new CreateOrderItemDto(menuItemType.getMenuItemTypeId(), 2)));

            String responseJson =
                    mockMvc.perform(
                                    post("/api/v1/order")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    objectMapper.writeValueAsString(createRequest)))
                            .andExpect(status().isCreated())
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

            UpdateOrderStatusDto updatedStatus = new UpdateOrderStatusDto(OrderStatus.IN_PROGRESS);
            Long orderId = objectMapper.readTree(responseJson).get("orderId").asLong();
            mockMvc.perform(
                            put("/api/v1/order/{orderId}/{staffId}/status", orderId, staff.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedStatus)))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/order/{id}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").value(orderId))
                    .andExpect(jsonPath("$.customerId").value(customer.getId()))
                    .andExpect(jsonPath("$.stationId").value(station.getId()))
                    .andExpect(jsonPath("$.staffId").value(staff.getId()))
                    .andExpect(jsonPath("$.orderStatus").value("IN_PROGRESS"));
        }

        /**
         * Verifies that updating the status of a non-existent order returns 404 Not Found, as the
         * service throws EntityNotFoundException when the order cannot be found.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateOrderStatus_returns404ForMissingOrder() throws Exception {
            UpdateOrderStatusDto updatedStatus = new UpdateOrderStatusDto(OrderStatus.IN_PROGRESS);
            Staff staff = saveTestStaff();

            mockMvc.perform(
                            put(
                                            "/api/v1/order/{orderId}/{staffId}/status",
                                            FAULTY_ID,
                                            staff.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedStatus)))
                    .andExpect(status().isNotFound());
        }

        /**
         * Verifies that updating an order's status with a null value returns 400 Bad Request, as
         * the @NotNull constraint on UpdateOrderStatusDto.orderStatus rejects null values.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateOrderStatus_returns400ForNullStatus() throws Exception {
            Customer customer = saveTestCustomer();
            Station station = saveTestStation();
            MenuItemType menuItemType = saveTestMenuItemType();
            Staff staff = saveTestStaff();

            CreatePurchaseOrderDto createRequest =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            station.getId(),
                            LocalTime.of(23, 57),
                            List.of(new CreateOrderItemDto(menuItemType.getMenuItemTypeId(), 2)));

            String responseJson =
                    mockMvc.perform(
                                    post("/api/v1/order")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    objectMapper.writeValueAsString(createRequest)))
                            .andExpect(status().isCreated())
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

            UpdateOrderStatusDto updatedStatus = new UpdateOrderStatusDto(null);
            Long orderId = objectMapper.readTree(responseJson).get("orderId").asLong();
            mockMvc.perform(
                            put("/api/v1/order/{orderId}/{staffId}/status", orderId, staff.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedStatus)))
                    .andExpect(status().isBadRequest());
        }

        /**
         * Verifies that transitioning a CANCELLED order to COMPLETED returns 409 Conflict, as the
         * service throws InvalidOrderStatusTransition when attempting to change the status of an
         * already cancelled order.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateOrderStatus_returns409ForInvalidTransition() throws Exception {
            Customer customer = saveTestCustomer();
            Station station = saveTestStation();
            MenuItemType menuItemType = saveTestMenuItemType();
            Staff staff = saveTestStaff();

            CreatePurchaseOrderDto createRequest =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            station.getId(),
                            LocalTime.of(23, 57),
                            List.of(new CreateOrderItemDto(menuItemType.getMenuItemTypeId(), 2)));

            String responseJson =
                    mockMvc.perform(
                                    post("/api/v1/order")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    objectMapper.writeValueAsString(createRequest)))
                            .andExpect(status().isCreated())
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

            UpdateOrderStatusDto cancelledStatus = new UpdateOrderStatusDto(OrderStatus.CANCELLED);
            Long orderId = objectMapper.readTree(responseJson).get("orderId").asLong();
            mockMvc.perform(
                            put("/api/v1/order/{orderId}/{staffId}/status", orderId, staff.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(cancelledStatus)))
                    .andExpect(status().isOk());

            UpdateOrderStatusDto updateStatusfromCancelled =
                    new UpdateOrderStatusDto(OrderStatus.COMPLETED);
            mockMvc.perform(
                            put("/api/v1/order/{orderId}/{staffId}/status", orderId, staff.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            objectMapper.writeValueAsString(
                                                    updateStatusfromCancelled)))
                    .andExpect(status().isConflict());
        }

        /**
         * Verifies that updating the status of a non-existent staff returns 404 Not Found, as the
         * service throws EntityNotFoundException when the staff preparing the order cannot be
         * found.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateOrderStatus_returns404ForMissingStaff() throws Exception {
            Customer customer = saveTestCustomer();
            Station station = saveTestStation();
            MenuItemType menuItemType = saveTestMenuItemType();

            CreatePurchaseOrderDto createRequest =
                    new CreatePurchaseOrderDto(
                            customer.getId(),
                            station.getId(),
                            LocalTime.of(23, 57),
                            List.of(new CreateOrderItemDto(menuItemType.getMenuItemTypeId(), 2)));

            String responseJson =
                    mockMvc.perform(
                                    post("/api/v1/order")
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    objectMapper.writeValueAsString(createRequest)))
                            .andExpect(status().isCreated())
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

            Long orderId = objectMapper.readTree(responseJson).get("orderId").asLong();
            UpdateOrderStatusDto updatedStatus = new UpdateOrderStatusDto(OrderStatus.IN_PROGRESS);

            mockMvc.perform(
                            put("/api/v1/order/{orderId}/{staffId}/status", orderId, FAULTY_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedStatus)))
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * Tests for the automatic order cancellation scheduler — scenarios where completed orders are
     * cancelled after exceeding the 15-minute pickup window, and where recent orders are left
     * untouched.
     */
    @Nested
    class AutoCancelationTests {
        /**
         * Verifies that a completed order whose pickup time was more than 15 minutes ago is
         * automatically cancelled when the scheduler runs.
         */
        @Test
        void cancelUncollectedOrders_cancelsOrdersPastPickUpTime() throws Exception {
            Customer customer = saveTestCustomer();
            Station station = saveTestStation();

            PurchaseOrder order =
                    orderRepository.save(
                            new PurchaseOrder(
                                    customer,
                                    station,
                                    LocalDate.now(),
                                    LocalTime.now().minusMinutes(20),
                                    OrderStatus.COMPLETED,
                                    5.00));

            orderAutoCancellation.cancelUncollectedOrders();

            PurchaseOrder updated = orderRepository.findById(order.getId()).orElseThrow();
            assertEquals(OrderStatus.CANCELLED, updated.getOrderStatus());
        }

        /**
         * Verifies that a completed order whose pickup time was less than 15 minutes ago is NOT
         * cancelled when the scheduler runs, as the grace period has not yet elapsed.
         */
        @Test
        void cancelUncollectedOrders_doesNotCancelRecentOrders() throws Exception {
            Customer customer = saveTestCustomer();
            Station station = saveTestStation();

            PurchaseOrder order =
                    orderRepository.save(
                            new PurchaseOrder(
                                    customer,
                                    station,
                                    LocalDate.now(),
                                    LocalTime.now().minusMinutes(5),
                                    OrderStatus.COMPLETED,
                                    5.00));

            orderAutoCancellation.cancelUncollectedOrders();

            PurchaseOrder updated = orderRepository.findById(order.getId()).orElseThrow();
            assertEquals(OrderStatus.COMPLETED, updated.getOrderStatus());
        }
    }

    /**
     * Creates and persists a test Customer entity with predefined data.
     *
     * @return the saved Customer entity
     */
    private Customer saveTestCustomer() {
        Customer customer = customerRepository.save(new Customer("Jane", "Grande", "07123456789"));
        return customer;
    }

    /**
     * Creates and persists a test Station entity with predefined opening hours.
     *
     * @return the saved Station entity
     */
    private Station saveTestStation() {
        Station station =
                stationRepository.save(
                        new Station("Cramlington", "08:00-23:58", "09:00-23:58", true));
        return station;
    }

    /**
     * Creates and persists a test MenuItem ("Latte") with two MenuItemTypes.
     *
     * @return the REGULAR MenuItemType entity
     */
    private MenuItemType saveTestMenuItemType() {
        MenuItem latte =
                menuItemRepository.save(new MenuItem("Latte", "Espresso with steamed milk", true));
        MenuItemType regular =
                menuItemTypeRepository.save(
                        new MenuItemType(latte, MenuItemSize.REGULAR, 2.50, true));
        menuItemTypeRepository.save(new MenuItemType(latte, MenuItemSize.LARGE, 3.00, true));
        return regular;
    }

    /**
     * Creates and persists a test Staff.
     *
     * @return the Staff entity
     */
    private Staff saveTestStaff() {
        Staff staff =
                staffRepository.save(
                        new Staff(
                                "barista1@example.com",
                                "Alex",
                                "Brown",
                                "staff_user",
                                "encoded-password"));
        return staff;
    }
}
