package org.coffeeshop.security.models;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

/**
 * Custom UserDetails implementation that includes staff ID and authorities. Used by Spring Security
 * for authentication and authorization.
 */
public class StaffUserDetails extends org.springframework.security.core.userdetails.User {
    private final Long id;

    /**
     * Constructs a StaffUserDetails with the given ID, username, password, and authorities.
     *
     * @param id the database ID of the staff member
     * @param username the login username
     * @param password the hashed password
     * @param authorities the granted authorities for this user
     */
    public StaffUserDetails(
            Long id,
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }

    /**
     * Returns the database ID of the staff member.
     *
     * @return the staff member's database ID
     */
    public Long getId() {
        return id;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
