package org.coffeeshop.usertests.customertests;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.coffeeshop.users.dtos.CreateCustomerDto;
import org.coffeeshop.users.dtos.UpdateCustomerDto;
import org.coffeeshop.users.models.Customer;
import org.coffeeshop.users.repositories.CustomerRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests for CustomerController. Uses Spring Boot's testing support with MockMvc to
 * perform integration tests on the customer endpoints. Tests are grouped by operation using @Nested
 * classes and are transactional to avoid persisting test data.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CustomerControllerTests {
    private static final Long FAULTY_ID = 999L;

    @Autowired private MockMvc mockMvc;
    @Autowired private EntityManager entityManager;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private CustomerRepository customerRepository;

    /**
     * Tests for POST /api/v1/customer — customer creation scenarios including happy path and
     * validation errors for null, empty, and invalid fields.
     */
    @Nested
    class CreateCustomerTests {
        /**
         * Verifies that creating a customer with valid data returns 201 Created with the correct
         * fields including customerId, customerFirstName, customerLastName, and
         * customerPhoneNumber. Also asserts the customer is persisted in the repository.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createCustomer_returnsCreatedCustomer() throws Exception {
            CreateCustomerDto request = new CreateCustomerDto("Jane", "Grande", "07123456789");

            mockMvc.perform(
                            post("/api/v1/customer")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.customerId").isNumber())
                    .andExpect(jsonPath("$.customerFirstName").value("Jane"))
                    .andExpect(jsonPath("$.customerLastName").value("Grande"))
                    .andExpect(jsonPath("$.customerPhoneNumber").value("07123456789"));

            assertTrue(
                    customerRepository.findAll().stream()
                            .anyMatch(
                                    customer ->
                                            "07123456789"
                                                    .equals(customer.getCustomerPhoneNumber())));
        }

        /**
         * Verifies that creating a customer with a null first name returns 400 Bad Request, as
         * the @NotBlank constraint on CreateCustomerDto.customerFirstName rejects null values.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createCustomer_returns400WhenFirstNameIsNull() throws Exception {
            CreateCustomerDto request = new CreateCustomerDto(null, "Grande", "07123456789");

            mockMvc.perform(
                            post("/api/v1/customer")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, customerRepository.count());
        }

        /**
         * Verifies that creating a customer with an empty first name returns 400 Bad Request, as
         * the @NotBlank constraint on CreateCustomerDto.customerFirstName rejects empty strings.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createCustomer_returns400WhenFirstNameIsEmpty() throws Exception {
            CreateCustomerDto request = new CreateCustomerDto("", "Grande", "07123456789");

            mockMvc.perform(
                            post("/api/v1/customer")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, customerRepository.count());
        }

        /**
         * Verifies that creating a customer with a null last name returns 400 Bad Request, as
         * the @NotBlank constraint on CreateCustomerDto.customerLastName rejects null values.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createCustomer_returns400WhenLastNameIsNull() throws Exception {
            CreateCustomerDto request = new CreateCustomerDto("Jane", null, "07123456789");

            mockMvc.perform(
                            post("/api/v1/customer")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, customerRepository.count());
        }

        /**
         * Verifies that creating a customer with an empty last name returns 400 Bad Request, as
         * the @NotBlank constraint on CreateCustomerDto.customerLastName rejects empty strings.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createCustomer_returns400WhenLastNameIsEmpty() throws Exception {
            CreateCustomerDto request = new CreateCustomerDto("Jane", "", "07123456789");

            mockMvc.perform(
                            post("/api/v1/customer")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, customerRepository.count());
        }

        /**
         * Verifies that creating a customer with a null phone number returns 400 Bad Request, as
         * the @NotNull constraint on CreateCustomerDto.customerPhoneNumber rejects null values.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createCustomer_returns400WhenPhoneIsNull() throws Exception {
            CreateCustomerDto request = new CreateCustomerDto("Jane", "Grande", null);

            mockMvc.perform(
                            post("/api/v1/customer")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, customerRepository.count());
        }

        /**
         * Verifies that creating a customer with an empty phone number returns 400 Bad Request, as
         * the @NotBlank constraint on CreateCustomerDto.customerPhoneNumber rejects empty strings.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createCustomer_returns400WhenPhoneIsEmpty() throws Exception {
            CreateCustomerDto request = new CreateCustomerDto("Jane", "Grande", "");

            mockMvc.perform(
                            post("/api/v1/customer")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, customerRepository.count());
        }

        /**
         * Verifies that creating a customer with a phone number that is too short returns 400 Bad
         * Request, as the @Size(min = 10) constraint on CreateCustomerDto.customerPhoneNumber
         * rejects short values.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void createCustomer_returns400WhenPhoneTooShort() throws Exception {
            CreateCustomerDto request = new CreateCustomerDto("Jane", "Grande", "0");

            mockMvc.perform(
                            post("/api/v1/customer")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            assertEquals(0, customerRepository.count());
        }
    }

    /**
     * Tests for GET /api/v1/customer — customer retrieval scenarios including get by ID, get all,
     * and 404 for missing customers.
     */
    @Nested
    class GetCustomerTests {
        /**
         * Verifies that GET /api/v1/customer returns 200 OK with a list containing all previously
         * created customers.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getAllCustomers_returnsListOfAllCustomers() throws Exception {
            customerRepository.save(new Customer("John", "Doe", "07000000000"));
            customerRepository.save(new Customer("Jasper", "Doe", "07000000001"));
            saveTestCustomer();

            mockMvc.perform(get("/api/v1/customer"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(
                            jsonPath(
                                    "$[*].customerPhoneNumber",
                                    containsInAnyOrder(
                                            "07123456789", "07000000000", "07000000001")));

            assertEquals(3, customerRepository.count());
        }

        /**
         * Verifies that retrieving an existing customer by ID returns 200 OK with the correct
         * fields including customerId, customerFirstName, and customerPhoneNumber.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getCustomerById_returnsCustomer() throws Exception {
            Customer customer = saveTestCustomer();

            mockMvc.perform(get("/api/v1/customer/{id}", customer.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerId").value(customer.getId()))
                    .andExpect(jsonPath("$.customerFirstName").value("Jane"))
                    .andExpect(jsonPath("$.customerPhoneNumber").value("07123456789"));
        }

        /**
         * Verifies that retrieving a non-existent customer ID returns 404 Not Found.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getCustomerById_returns404ForMissingCustomer() throws Exception {
            mockMvc.perform(get("/api/v1/customer/{id}", FAULTY_ID))
                    .andExpect(status().isNotFound());
        }

        /**
         * Verifies that GET /api/v1/customer returns 200 OK with an empty list when no customers
         * exist.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void getCustomer_returnsEmptyListWhenNoCustomers() throws Exception {
            mockMvc.perform(get("/api/v1/customer"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    /**
     * Tests for PUT /api/v1/customer/{id} — customer update scenarios including happy path, 404 for
     * missing customer, and validation errors.
     */
    @Nested
    class UpdateCustomerTests {
        /**
         * Verifies that updating an existing customer with valid data returns 200 OK, and that a
         * subsequent GET confirms the updated fields have been persisted.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateCustomer_returns200() throws Exception {
            Customer savedCustomer = saveTestCustomer();
            UpdateCustomerDto updatedCustomer = new UpdateCustomerDto("John", "Doe", "07011112222");

            mockMvc.perform(
                            put("/api/v1/customer/{id}", savedCustomer.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedCustomer)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerFirstName").value("John"));

            mockMvc.perform(get("/api/v1/customer/{id}", savedCustomer.getId()))
                    .andExpect(jsonPath("$.customerId").value(savedCustomer.getId()))
                    .andExpect(jsonPath("$.customerFirstName").value("John"))
                    .andExpect(jsonPath("$.customerLastName").value("Doe"))
                    .andExpect(jsonPath("$.customerPhoneNumber").value("07011112222"));

            assertEquals(1, customerRepository.count());
        }

        /**
         * Verifies that updating a non-existent customer ID returns 404 Not Found, and that a
         * subsequent GET confirms the existing customer was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateCustomer_returns404ForMissingCustomer() throws Exception {
            Customer savedCustomer = saveTestCustomer();
            UpdateCustomerDto updatedCustomer = new UpdateCustomerDto("John", "Doe", "07011112222");

            mockMvc.perform(
                            put("/api/v1/customer/{id}", FAULTY_ID)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedCustomer)))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get("/api/v1/customer/{id}", savedCustomer.getId()))
                    .andExpect(jsonPath("$.customerId").value(savedCustomer.getId()))
                    .andExpect(jsonPath("$.customerFirstName").value("Jane"))
                    .andExpect(jsonPath("$.customerLastName").value("Grande"))
                    .andExpect(jsonPath("$.customerPhoneNumber").value("07123456789"));
        }

        /**
         * Verifies that updating a customer with a null first name returns 400 Bad Request, as
         * the @NotBlank constraint on UpdateCustomerDto.customerFirstName rejects null values, and
         * that a subsequent GET confirms the existing customer was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateCustomer_returns400ForNullFirstName() throws Exception {
            Customer savedCustomer = saveTestCustomer();
            UpdateCustomerDto updatedCustomer = new UpdateCustomerDto(null, "Doe", "07011112222");

            mockMvc.perform(
                            put("/api/v1/customer/{id}", savedCustomer.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedCustomer)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/customer/{id}", savedCustomer.getId()))
                    .andExpect(jsonPath("$.customerId").value(savedCustomer.getId()))
                    .andExpect(jsonPath("$.customerFirstName").value("Jane"))
                    .andExpect(jsonPath("$.customerLastName").value("Grande"))
                    .andExpect(jsonPath("$.customerPhoneNumber").value("07123456789"));
        }

        /**
         * Verifies that updating a customer with an empty first name returns 400 Bad Request, as
         * the @NotBlank constraint on UpdateCustomerDto.customerFirstName rejects empty strings,
         * and that a subsequent GET confirms the existing customer was not modified.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void updateCustomer_returns400ForEmptyFirstName() throws Exception {
            Customer savedCustomer = saveTestCustomer();
            UpdateCustomerDto updatedCustomer = new UpdateCustomerDto("", "Doe", "07011112222");

            mockMvc.perform(
                            put("/api/v1/customer/{id}", savedCustomer.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updatedCustomer)))
                    .andExpect(status().isBadRequest());

            mockMvc.perform(get("/api/v1/customer/{id}", savedCustomer.getId()))
                    .andExpect(jsonPath("$.customerId").value(savedCustomer.getId()))
                    .andExpect(jsonPath("$.customerFirstName").value("Jane"))
                    .andExpect(jsonPath("$.customerLastName").value("Grande"))
                    .andExpect(jsonPath("$.customerPhoneNumber").value("07123456789"));
        }
    }

    /**
     * Tests for DELETE /api/v1/customer/{id} — customer deletion scenarios including happy path
     * with follow-up verification and 404 for missing customers.
     */
    @Nested
    class DeleteCustomerTests {
        /**
         * Verifies that deleting an existing customer returns 204 No Content, and that a subsequent
         * GET returns 404. Also asserts that the customer no longer exists in the repository.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void deleteCustomer_returns204() throws Exception {
            Customer customer = saveTestCustomer();

            mockMvc.perform(delete("/api/v1/customer/{id}", customer.getId()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/v1/customer/{id}", customer.getId()))
                    .andExpect(status().isNotFound());

            assertFalse(customerRepository.existsById(customer.getId()));
        }

        /**
         * Verifies that deleting a non-existent customer returns 404 Not Found.
         *
         * @throws Exception if the MockMvc request fails
         */
        @Test
        void deleteCustomer_returns404ForMissingCustomer() throws Exception {
            mockMvc.perform(delete("/api/v1/customer/{id}", FAULTY_ID))
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * Creates and persists a test Customer entity with predefined data, then flushes and clears the
     * persistence context to ensure fresh loading.
     *
     * @return the saved Customer entity
     */
    private Customer saveTestCustomer() {
        Customer customer = customerRepository.save(new Customer("Jane", "Grande", "07123456789"));

        entityManager.flush();
        entityManager.clear();

        return customer;
    }
}
