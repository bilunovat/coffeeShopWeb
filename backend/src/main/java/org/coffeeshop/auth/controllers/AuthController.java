package org.coffeeshop.auth.controllers;

import jakarta.validation.Valid;
import org.coffeeshop.auth.dtos.AuthResponseDto;
import org.coffeeshop.auth.dtos.LoginRequestDto;
import org.coffeeshop.security.StaffUserDetails;
import org.coffeeshop.security.services.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Handles authentication endpoints. */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Creates an AuthController with the required authentication and JWT dependencies.
     *
     * @param authenticationManager the authentication manager for verifying credentials
     * @param jwtService the service for generating JWT tokens
     */
    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Authenticates a user and returns a JWT for subsequent requests.
     *
     * @param request login credentials
     * @return token response including bearer token and role
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.username(), request.password()));

        StaffUserDetails userDetails = (StaffUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        String role =
                authentication.getAuthorities().stream()
                        .findFirst()
                        .map(GrantedAuthority::getAuthority)
                        .orElse("ROLE_USER");

        return ResponseEntity.ok(
                new AuthResponseDto(token, userDetails.getUsername(), userDetails.getId(), role));
    }
}
