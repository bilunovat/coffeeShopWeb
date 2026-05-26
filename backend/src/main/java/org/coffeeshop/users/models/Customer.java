package org.coffeeshop.users.models;

import jakarta.persistence.*;

/** JPA entity representing a customer in the coffee shop system. */
@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_firstname", nullable = false)
    private String customerFirstName;

    @Column(name = "customer_lastname", nullable = false)
    private String customerLastName;

    @Column(name = "customer_phone_number", nullable = false)
    private String customerPhoneNumber;

    protected Customer() {}

    /**
     * Creates a new Customer entity without an explicit ID.
     *
     * @param customerFirstName the customer's first name
     * @param customerLastName the customer's last name
     * @param customerPhoneNumber the customer's phone number
     */
    public Customer(String customerFirstName, String customerLastName, String customerPhoneNumber) {
        this(null, customerFirstName, customerLastName, customerPhoneNumber);
    }

    /**
     * Creates a new Customer entity with an explicit ID.
     *
     * @param customerId the primary key
     * @param customerFirstName the customer's first name
     * @param customerLastName the customer's last name
     * @param customerPhoneNumber the customer's phone number
     */
    public Customer(
            Long customerId,
            String customerFirstName,
            String customerLastName,
            String customerPhoneNumber) {
        this.customerId = customerId;
        this.customerFirstName = customerFirstName;
        this.customerLastName = customerLastName;
        this.customerPhoneNumber = customerPhoneNumber;
    }

    /**
     * Returns the customer's database ID.
     *
     * @return the customer ID
     */
    public Long getId() {
        return customerId;
    }

    /**
     * Returns the customer's first name.
     *
     * @return the first name
     */
    public String getCustomerFirstName() {
        return customerFirstName;
    }

    /**
     * Returns the customer's last name.
     *
     * @return the last name
     */
    public String getCustomerLastName() {
        return customerLastName;
    }

    /**
     * Returns the customer's phone number.
     *
     * @return the phone number
     */
    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }
}
