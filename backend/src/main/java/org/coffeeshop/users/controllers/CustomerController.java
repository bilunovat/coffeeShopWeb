package org.coffeeshop.users.controllers;

import jakarta.validation.Valid;
import java.util.List;
import org.coffeeshop.users.dtos.CreateCustomerDto;
import org.coffeeshop.users.dtos.CustomerDto;
import org.coffeeshop.users.dtos.UpdateCustomerDto;
import org.coffeeshop.users.services.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller that exposes CRUD endpoints for managing customers in the Coffee Shop system.
 *
 * <p>This controller delegates business logic to {@link CustomerService} and focuses on handling
 * HTTP requests and responses. All endpoints are versioned under {@code /api/v1/customer}. The
 * operations include creating new customers, retrieving existing customers, updating their details,
 * and deleting them.
 */
@RestController
@RequestMapping("/api/v1/customer")
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Constructs a new {@code CustomerController} with the required customer service.
     *
     * @param customerService the service that encapsulates customer-related business logic; must
     *     not be {@code null}
     */
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Creates a new customer based on the data provided in the request body.
     *
     * <p>The incoming {@link CustomerDto} is validated and passed to the {@link CustomerService},
     * which persists the new customer in the Repository. On success, the created customer
     * (including any generated identifiers) is returned with HTTP status {@link HttpStatus#CREATED
     * 201 (Created)}.
     *
     * @param dto the DTO containing the details of the customer to create; must not be {@code null}
     * @return an HTTP 201 (Created) response containing the newly created customer DTO
     */
    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CreateCustomerDto dto) {
        CustomerDto created = customerService.createCustomer(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Retrieves all customers currently stored in the system.
     *
     * <p>This endpoint returns a list of {@link CustomerDto} objects representing all customers. If
     * no customers exist, an empty list is returned. The response uses HTTP status {@link
     * HttpStatus#OK 200 (OK)}.
     *
     * @return an HTTP 200 (OK) response containing a list of all customer DTOs (possibly empty)
     */
    @GetMapping
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        return new ResponseEntity<>(customerService.getAllCustomers(), HttpStatus.OK);
    }

    /**
     * Retrieves a single customer by its unique identifier.
     *
     * <p>The request is delegated to {@link CustomerService#getCustomerById(Long)}. If the customer
     * does not exist, the service layer is expected to handle this case (for example, by throwing
     * an exception that is mapped to a {@code 404 Not Found} response).
     *
     * @param id the unique identifier of the customer to retrieve; must not be {@code null}
     * @return an HTTP 200 (OK) response containing the customer DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    /**
     * Updates an existing customer with new details.
     *
     * <p>The method delegates to {@link CustomerService#updateCustomer(Long, CustomerDto)} to apply
     * the update. On success, the updated {@link CustomerDto} is returned with HTTP status {@link
     * HttpStatus#OK 200 (OK)}. If the customer does not exist, the service layer should signal this
     * appropriately (for example, by throwing an exception that results in a {@code 404 Not Found}
     * response).
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(
            @PathVariable("id") Long id, @Valid @RequestBody UpdateCustomerDto dto) {

        return ResponseEntity.ok(customerService.updateCustomer(id, dto));
    }

    /**
     * Deletes an existing customer identified by the given id.
     *
     * <p>The deletion is performed by {@link CustomerService#deleteCustomer(Long)}, which returns a
     * human-readable message describing the result. This message is wrapped in a JSON object under
     * the {@code "message"} key and returned with HTTP status {@link HttpStatus#OK 200 (OK)}. If
     * the customer does not exist, the service layer should handle this case (for example, by
     * raising an exception that maps to a {@code 404 Not Found} response).
     *
     * @param id the unique identifier of the customer to delete; must not be {@code null}
     * @return an HTTP 200 (OK) response containing a JSON object with a {@code "message"} field
     *     describing the outcome
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable("id") Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
