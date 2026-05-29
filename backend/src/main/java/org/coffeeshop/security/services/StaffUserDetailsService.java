package org.coffeeshop.security.services;

import java.util.List;

import org.coffeeshop.security.models.StaffUserDetails;
import org.coffeeshop.security.utils.SecurityRoleUtils;
import org.coffeeshop.users.models.Staff;
import org.coffeeshop.users.repositories.StaffRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Loads staff credentials and authorities for Spring Security authentication. */
@Service
public class StaffUserDetailsService implements UserDetailsService {

    private final StaffRepository staffRepository;

    /**
     * Constructs the service with the staff repository dependency.
     *
     * @param staffRepository repository for staff entity access
     */
    public StaffUserDetailsService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    /**
     * Loads user details by username from staff repository.
     *
     * @param username login username
     * @return spring security user details
     * @throws UsernameNotFoundException when user does not exist
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Staff staff =
                staffRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () -> new UsernameNotFoundException("User not found: " + username));

        String role = SecurityRoleUtils.toAuthority(staff.getRole());

        return new StaffUserDetails(
                staff.getId(),
                staff.getUsername(),
                staff.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(role)));
    }
}
