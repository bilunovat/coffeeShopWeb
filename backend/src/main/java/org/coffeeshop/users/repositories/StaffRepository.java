package org.coffeeshop.users.repositories;

import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import org.coffeeshop.users.models.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Staff entities in the database. It extends JpaRepository to
 * provide CRUD operations and includes methods to check for the existence of a staff by username.
 */
@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {
    /**
     * Checks whether a staff member with the given username exists.
     *
     * @param username the username to check
     * @return true if a staff member with the username exists
     */
    boolean existsByUsername(@NotNull String username);

    /**
     * Finds a staff member by their username.
     *
     * @param username the username to search for
     * @return an Optional containing the matching staff member, or empty if not found
     */
    Optional<Staff> findByUsername(@NotNull String username);
}
