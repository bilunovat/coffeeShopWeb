package org.coffeeshop.users.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** DTO for updating an existing customer with validation constraints. */
public record UpdateCustomerDto(
        @NotNull @NotBlank String customerFirstName,
        @NotNull @NotBlank String customerLastName,
        @NotNull @NotBlank String customerPhoneNumber) {}
