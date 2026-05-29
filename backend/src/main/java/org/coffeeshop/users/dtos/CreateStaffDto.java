package org.coffeeshop.users.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * this is a data transfer object class for creating a new staff member its main function is to
 * ensure that only the necessary data is sent to the client and to provide a clear structure for
 * the data being transferred it is made up of Attributes in the staff entity
 */
public record CreateStaffDto(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 1, max = 50) String firstName,
        @NotBlank @Size(min = 1, max = 50) String lastName,
        @NotBlank String role,
        @NotBlank @Size(min = 6, max = 25) String password) {}
