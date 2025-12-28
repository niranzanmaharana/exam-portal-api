package com.niranzan.exam.config;

import com.niranzan.exam.entity.User;
import com.niranzan.exam.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@examportal.com";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultAdmin();
    }

    private void initializeDefaultAdmin() {
        // Check if admin user already exists
        if (userRepository.existsByUsername(DEFAULT_ADMIN_USERNAME)) {
            log.info("Default admin user already exists. Skipping creation.");
            return;
        }

        try {
            // Create default admin user
            User admin = new User();
            admin.setUsername(DEFAULT_ADMIN_USERNAME);
            admin.setEmail(DEFAULT_ADMIN_EMAIL);
            admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
            admin.setRole(User.Role.ADMIN);
            admin.setFirstName("Super");
            admin.setLastName("Admin");
            admin.setStatus(User.UserStatus.ACTIVE);

            userRepository.save(admin);
            log.info("==========================================");
            log.info("Default Admin User Created Successfully!");
            log.info("Username: {}", DEFAULT_ADMIN_USERNAME);
            log.info("Password: {}", DEFAULT_ADMIN_PASSWORD);
            log.info("Email: {}", DEFAULT_ADMIN_EMAIL);
            log.info("Role: ADMIN");
            log.info("==========================================");
            log.warn("IMPORTANT: Please change the default password after first login!");
            log.info("==========================================");
        } catch (Exception e) {
            log.error("Error creating default admin user: {}", e.getMessage(), e);
        }
    }
}

