package org.coffeeshop.auth.dtos;

import jakarta.validation.constraints.NotBlank;

/** Request payload for username/password login. */
public record LoginRequestDto(@NotBlank String username, @NotBlank String password) {}
