package com.niranzan.exam.service;

import com.niranzan.exam.entity.PasswordResetToken;
import com.niranzan.exam.entity.User;
import com.niranzan.exam.repository.PasswordResetTokenRepository;
import com.niranzan.exam.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Excluding confusing characters (0, O, I, 1)

    /**
     * Generates a unique 5-character alphanumeric registration code
     */
    private String generateRegistrationCode() {
        Random random = new Random();
        String code;
        int attempts = 0;
        int maxAttempts = 100;

        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                sb.append(ALPHANUMERIC_CHARS.charAt(random.nextInt(ALPHANUMERIC_CHARS.length())));
            }
            code = sb.toString();
            attempts++;
        } while (userRepository.existsByRegistrationCode(code) && attempts < maxAttempts);

        if (attempts >= maxAttempts) {
            throw new RuntimeException("Failed to generate unique registration code after " + maxAttempts + " attempts");
        }

        return code;
    }

    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Generate registration code for CANDIDATE users
        if (user.getRole() == User.Role.CANDIDATE) {
            String registrationCode = generateRegistrationCode();
            user.setRegistrationCode(registrationCode);
        }
        
        return userRepository.save(user);
    }

    /**
     * Finds an existing student user by registration code.
     * Students must register with email and password first - this method does not create new users.
     * 
     * @param registrationCode The registration code to search for
     * @return The user if found
     * @throws RuntimeException if user is not found
     */
    public User findStudentByRegistrationCode(String registrationCode) {
        Optional<User> userOpt = userRepository.findByRegistrationCode(registrationCode);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Ensure user has registration code (should already have one, but just in case)
            if (user.getRegistrationCode() == null || user.getRegistrationCode().isEmpty()) {
                user.setRegistrationCode(generateRegistrationCode());
                user = userRepository.save(user);
            }
            return user;
        }
        throw new RuntimeException("Student not found with registration code: " + registrationCode + ". Please register first.");
    }

    /**
     * @deprecated This method is deprecated. Students must register with email and password.
     * Use findStudentByRegistrationCode() instead, or ensure students register first.
     */
    @Deprecated
    public User createOrFindStudent(String studentName, String registrationNumber) {
        // Try to find by registration code first
        try {
            return findStudentByRegistrationCode(registrationNumber);
        } catch (RuntimeException e) {
            // If not found, throw error - students must register first
            throw new RuntimeException("Student not found. Please register with email and password first. Registration code: " + registrationNumber);
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(User user) {
        // If password is being updated, encode it
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // Check if password is already encoded (starts with $2a$ or $2b$ for BCrypt)
            if (!user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$")) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        } else {
            // If password is not provided, keep the existing password
            Optional<User> existingUser = userRepository.findById(user.getId());
            if (existingUser.isPresent()) {
                user.setPassword(existingUser.get().getPassword());
            }
        }
        return userRepository.save(user);
    }

    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateProfile(Long userId, String firstName, String lastName, String email, String mobileNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if email is being changed and if it's already taken by another user
        if (email != null && !email.equals(user.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(email);
        }

        if (firstName != null) {
            user.setFirstName(firstName);
        }
        if (lastName != null) {
            user.setLastName(lastName);
        }
        if (mobileNumber != null) {
            user.setMobileNumber(mobileNumber);
        }

        return userRepository.save(user);
    }

    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }

        // Update to new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    public void updateUserStatus(Long userId, User.UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(status);
        userRepository.save(user);
    }

    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public PasswordResetToken saveResetToken(Long userId, LocalDateTime expiry) {
        // Mark all existing tokens for this user as used
        passwordResetTokenRepository.markAllTokensAsUsedForUser(userId);

        // Create new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUserId(userId);
        resetToken.setExpiryDate(expiry);
        resetToken.setUsed(false);

        return passwordResetTokenRepository.save(resetToken);
    }

    public Optional<PasswordResetToken> findByResetToken(String token) {
        return passwordResetTokenRepository.findByTokenAndUsedFalse(token);
    }

    @Transactional
    public void markTokenAsUsed(String token) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isPresent()) {
            PasswordResetToken resetToken = tokenOpt.get();
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);
        }
    }

    public void deleteExpiredTokens() {
        passwordResetTokenRepository.deleteExpiredTokens(java.time.LocalDateTime.now());
    }
}

