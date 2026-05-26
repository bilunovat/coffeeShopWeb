package org.coffeeshop.users.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** DTO for creating a new customer with validation constraints. */
public record CreateCustomerDto(
        @NotNull @NotBlank @Size(min = 1, max = 50) String customerFirstName,
        @NotNull @NotBlank @Size(min = 1, max = 50) String customerLastName,
        @NotNull @Pattern(regexp = "\\d{11}", message = "Phone number must be exactly 11 digits")
                String customerPhoneNumber) {}
