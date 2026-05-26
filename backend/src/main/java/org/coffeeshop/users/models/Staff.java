package org.coffeeshop.users.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/** JPA entity representing a staff member in the coffee shop system. */
@Entity
@Table(name = "staff")
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Long staffId;

    // used for login
    @Column(nullable = false, unique = true)
    @NotNull
    private String username;

    // basic identity
    @Column(name = "staff_firstname", nullable = false)
    @NotNull
    private String staffFirstName;

    @Column(name = "staff_lastname", nullable = false)
    @NotNull
    private String staffLastName;

    // e.g. "BARISTA", "MANAGER", "ADMIN"
    @Column(name = "staff_role", nullable = false)
    private String staffRole;

    // hashed password (never expose this in DTOs)
    @Column(name = "password_hash", nullable = false)
    @NotNull
    private String passwordHash;

    protected Staff() {}

    /**
     * Creates a new Staff entity without an explicit ID.
     *
     * @param username the login username
     * @param staffFirstName the staff member's first name
     * @param staffLastName the staff member's last name
     * @param staffRole the staff member's role
     * @param passwordHash the hashed password
     */
    public Staff(
            String username,
            String staffFirstName,
            String staffLastName,
            String staffRole,
            String passwordHash) {
        this(null, username, staffFirstName, staffLastName, staffRole, passwordHash);
    }

    /**
     * Creates a new Staff entity with an explicit ID.
     *
     * @param staffId the primary key
     * @param username the login username
     * @param staffFirstName the staff member's first name
     * @param staffLastName the staff member's last name
     * @param staffRole the staff member's role
     * @param passwordHash the hashed password
     */
    public Staff(
            Long staffId,
            String username,
            String staffFirstName,
            String staffLastName,
            String staffRole,
            String passwordHash) {
        this.staffId = staffId;
        this.username = username;
        this.staffFirstName = staffFirstName;
        this.staffLastName = staffLastName;
        this.staffRole = staffRole;
        this.passwordHash = passwordHash;
    }

    // === getters ===

    /**
     * Returns the staff member's database ID.
     *
     * @return the staff ID
     */
    public Long getId() {
        return staffId;
    }

    /**
     * Returns the staff member's login username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the staff member's first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return staffFirstName;
    }

    /**
     * Returns the staff member's last name.
     *
     * @return the last name
     */
    public String getLastName() {
        return staffLastName;
    }

    /**
     * Returns the staff member's role.
     *
     * @return the role
     */
    public String getRole() {
        return staffRole;
    }

    /**
     * Returns the staff member's hashed password.
     *
     * @return the password hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }
}
