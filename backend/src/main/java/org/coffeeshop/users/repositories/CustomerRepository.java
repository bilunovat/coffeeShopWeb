package org.coffeeshop.users.repositories;

import java.util.List;
import org.coffeeshop.users.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository interface for managing Customer entities in the database. */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    /**
     * Finds a customer by their phone number.
     *
     * @param customerPhoneNumber the phone number of the customer
     * @return the customer entity matching the given phone number
     */
    List<Customer> findByCustomerPhoneNumber(String customerPhoneNumber);

    /**
     * Checks if a customer exists with the given phone number.
     *
     * @param customerPhoneNumber the phone number to check for existence
     * @return true if a customer with the given phone number exists, false otherwise
     */
    boolean existsByCustomerPhoneNumber(String customerPhoneNumber);
}
