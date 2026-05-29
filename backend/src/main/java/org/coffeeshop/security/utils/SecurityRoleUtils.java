package org.coffeeshop.security.utils;

import java.util.Locale;

/** Utility methods for normalizing and formatting security role names. */
public final class SecurityRoleUtils {
    private static final String ROLE_PREFIX = "ROLE_";

    private SecurityRoleUtils() {}

    /**
     * Normalizes a role string to uppercase and ensures it is prefixed with {@code ROLE_}.
     *
     * @param role the raw role string
     * @return the normalized authority string prefixed with {@code ROLE_}
     */
    public static String toAuthority(String role) {
        String normalized = role == null ? "USER" : role.trim().toUpperCase(Locale.ROOT);
        if (!normalized.startsWith(ROLE_PREFIX)) {
            normalized = ROLE_PREFIX + normalized;
        }
        return normalized;
    }
}
