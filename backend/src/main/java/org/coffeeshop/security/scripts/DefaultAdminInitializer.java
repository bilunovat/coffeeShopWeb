package org.coffeeshop.security.scripts;

import org.coffeeshop.users.models.Staff;
import org.coffeeshop.users.repositories.StaffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Bootstraps a default admin user when the database is empty and the feature is enabled. */
@Component
public class DefaultAdminInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DefaultAdminInitializer.class);

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.enabled:false}")
    private boolean enabled;

    @Value("${app.bootstrap.admin.username:admin}")
    private String username;

    @Value("${app.bootstrap.admin.password:Admin123!}")
    private String password;

    @Value("${app.bootstrap.admin.first-name:System}")
    private String firstName;

    @Value("${app.bootstrap.admin.last-name:Admin}")
    private String lastName;

    @Value("${app.bootstrap.admin.role:ADMIN}")
    private String role;

    /**
     * Constructs the initializer with staff repository and password encoder dependencies.
     *
     * @param staffRepository repository for staff entity access
     * @param passwordEncoder encoder for hashing the default admin password
     */
    public DefaultAdminInitializer(
            StaffRepository staffRepository, PasswordEncoder passwordEncoder) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates the default admin user on startup if enabled and no staff exist.
     *
     * @param args command-line arguments passed at startup
     */
    @Override
    public void run(String... args) {
        if (!enabled || staffRepository.count() > 0) {
            return;
        }

        Staff admin =
                new Staff(username, firstName, lastName, role, passwordEncoder.encode(password));
        staffRepository.save(admin);

        logger.info(
                "Bootstrapped default admin user '{}'. Change its password after first login.",
                username);
    }
}
