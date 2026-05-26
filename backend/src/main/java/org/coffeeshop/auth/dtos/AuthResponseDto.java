package org.coffeeshop.auth.dtos;

/** Response payload returned after successful authentication. */
public record AuthResponseDto(
        String token, String tokenType, String username, Long id, String role) {
    /**
     * Convenience constructor that defaults the token type to "Bearer".
     *
     * @param token the JWT token string
     * @param username the authenticated user's username
     * @param id the authenticated user's database ID
     * @param role the authenticated user's role authority
     */
    public AuthResponseDto(String token, String username, Long id, String role) {
        this(token, "Bearer", username, id, role);
    }
}
