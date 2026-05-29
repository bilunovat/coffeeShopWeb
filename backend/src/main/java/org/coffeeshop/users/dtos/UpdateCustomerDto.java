package org.coffeeshop.users.dtos;

import jakarta.validation.constraints.NotBlank;

/** DTO for updating an existing customer with validation constraints. */
public record UpdateCustomerDto(
        @NotBlank String customerFirstName,
        @NotBlank String customerLastName,
        @NotBlank String customerPhoneNumber) {}
