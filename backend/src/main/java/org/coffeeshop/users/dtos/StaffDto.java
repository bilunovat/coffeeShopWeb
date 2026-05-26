package org.coffeeshop.users.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * this is adata transfer object class for the staff entity its main function is to ensure that only
 * the necessary data is sent to the client and to provide a clear structure for the data being
 * transferred it is made up of Attributes in the staff entity
 */
public record StaffDto(
        Long id,
        String username,
        String firstName,
        String lastName,
        String role,
        @JsonProperty(value = "password", access = JsonProperty.Access.WRITE_ONLY)
                String password) {}
