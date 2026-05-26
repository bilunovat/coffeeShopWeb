package org.coffeeshop.users.dtos;

/**
 * this is a data transfer object class for the customer entity its main function is to ensure that
 * only the necessary data is sent to the client and to provide a clear structure for the data being
 * transferred it is made up of Attributes in the customer entity
 */
public record CustomerDto(
        Long customerId,
        String customerFirstName,
        String customerLastName,
        String customerPhoneNumber) {}
