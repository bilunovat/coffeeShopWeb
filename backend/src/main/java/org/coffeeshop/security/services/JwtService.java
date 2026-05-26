package org.coffeeshop.security.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/** Provides JWT creation, parsing, and validation utilities. */
@Service
public class JwtService {

    private static final int MIN_HMAC_KEY_BYTES = 32;

    private final SecretKey signingKey;
    private final long jwtExpirationMs;

    /**
     * Constructs the JwtService with the signing secret and token expiration.
     *
     * @param jwtSecret HMAC secret key value
     * @param jwtExpirationMs token lifetime in milliseconds
     */
    public JwtService(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration-ms:3600000}") long jwtExpirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(validateSecret(jwtSecret));
        this.jwtExpirationMs = validateExpiration(jwtExpirationMs);
    }

    /**
     * Extracts username (subject) from a JWT.
     *
     * @param token signed JWT token
     * @return username stored in token subject
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a claim from a JWT using the provided resolver.
     *
     * @param token signed JWT token
     * @param claimsResolver function to resolve a value from claims
     * @param <T> resolved claim type
     * @return resolved claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a signed JWT for an authenticated user.
     *
     * @param userDetails authenticated user details
     * @return compact JWT string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        String role =
                userDetails.getAuthorities().stream()
                        .findFirst()
                        .map(Object::toString)
                        .orElse("ROLE_USER");
        extraClaims.put("role", role);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validates token ownership and expiration.
     *
     * @param token signed JWT token
     * @param userDetails expected user details
     * @return true when token belongs to user and is not expired
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                && userDetails.isEnabled()
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }

    private byte[] validateSecret(String secret) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Invalid JWT configuration: 'app.jwt.secret' must be provided and must not be blank.");
        }

        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_HMAC_KEY_BYTES) {
            throw new IllegalArgumentException(
                    "Invalid JWT configuration: 'app.jwt.secret' must be at least "
                            + MIN_HMAC_KEY_BYTES
                            + " bytes when encoded as UTF-8 for HMAC signing.");
        }

        return secretBytes;
    }

    private long validateExpiration(long expirationMs) {
        if (expirationMs <= 0) {
            throw new IllegalArgumentException(
                    "Invalid JWT configuration: 'app.jwt.expiration-ms' must be greater than 0.");
        }
        return expirationMs;
    }
}
