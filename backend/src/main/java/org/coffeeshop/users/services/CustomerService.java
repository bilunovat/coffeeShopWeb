package org.coffeeshop.users.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.coffeeshop.exceptions.userexceptions.CustomerServiceException;
import org.coffeeshop.users.dtos.CreateCustomerDto;
import org.coffeeshop.users.dtos.CustomerDto;
import org.coffeeshop.users.dtos.UpdateCustomerDto;
import org.coffeeshop.users.models.Customer;
import org.coffeeshop.users.repositories.CustomerRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/** Service for managing customer data in the coffee shop. */
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Creates a new CustomerService instance.
     *
     * @param customerRepository the repository for customer database operations
     */
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Create a new customer from the provided DTO.
     *
     * @param dto the customer data transfer object containing the information needed to create a
     *     new customer
     * @return the created customer as a DTO
     * @throws CustomerServiceException if the customer could not be created
     */
    public CustomerDto createCustomer(CreateCustomerDto dto) {
        try {
            Customer newCustomer = fromDtoCreate(dto);
            if (newCustomer == null) {
                throw new IllegalArgumentException("Customer data is invalid");
            }
            Customer saved = customerRepository.save(newCustomer);
            return toDto(saved);
        } catch (DataAccessException e) {
            throw new CustomerServiceException("Could not create customer", e);
        }
    }

    /**
     * Retrieves all customers from the database.
     *
     * @return a list of all customers as DTOs
     */
    public List<CustomerDto> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream().map(this::toDto).collect(Collectors.toList());
    }

    /** Find customer by id. */
    public CustomerDto getCustomerById(Long id) {
        Customer customer =
                customerRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new EntityNotFoundException("Customer not found: " + id));
        return toDto(customer);
    }

    /**
     * Updates an existing customer identified by the given ID.
     *
     * @param id the unique identifier of the customer to update
     * @param dto the updated customer data
     * @return the updated customer as a DTO
     */
    public CustomerDto updateCustomer(Long id, UpdateCustomerDto dto) {
        Customer existing =
                customerRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new EntityNotFoundException("Customer not found: " + id));

        Customer updated =
                new Customer(
                        existing.getId(),
                        dto.customerFirstName(),
                        dto.customerLastName(),
                        dto.customerPhoneNumber());

        Customer saved = customerRepository.save(updated);
        return toDto(saved);
    }

    /**
     * Deletes a customer by ID.
     *
     * @param id the ID of the customer to delete
     */
    @Transactional
    public void deleteCustomer(Long id) {
        Customer entity =
                customerRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new EntityNotFoundException("Customer not found: " + id));
        customerRepository.delete(entity);
    }

    /**
     * Finds customers by their phone number.
     *
     * @param phoneNumber the phone number to search for
     * @return a list of matching customer entities
     */
    public List<Customer> findCustomersByPhone(String phoneNumber) {
        return customerRepository.findByCustomerPhoneNumber(phoneNumber);
    }

    /** Internal lookup returning the entity (for use by other services). */
    public Customer getCustomerEntityById(Long id) {
        return customerRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + id));
    }

    /** Mapping helpers, similar to StaffService. */
    private CustomerDto toDto(Customer customer) {
        return new CustomerDto(
                customer.getId(),
                customer.getCustomerFirstName(),
                customer.getCustomerLastName(),
                customer.getCustomerPhoneNumber());
    }

    private Customer fromDtoCreate(CreateCustomerDto dto) {
        return new Customer(
                dto.customerFirstName(), dto.customerLastName(), dto.customerPhoneNumber());
    }
}
