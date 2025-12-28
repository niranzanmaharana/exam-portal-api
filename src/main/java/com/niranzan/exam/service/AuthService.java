package com.niranzan.exam.service;

import com.niranzan.exam.dto.ForgotPasswordRequest;
import com.niranzan.exam.dto.LoginRequest;
import com.niranzan.exam.dto.LoginResponse;
import com.niranzan.exam.dto.RegisterRequest;
import com.niranzan.exam.dto.ResetPasswordRequest;
import com.niranzan.exam.entity.PasswordResetToken;
import com.niranzan.exam.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Get user details after authentication
        User user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate token with user details included
        String token = jwtService.generateTokenWithUserDetails(user);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setId(user.getId());
        response.setRegistrationCode(user.getRegistrationCode());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        return response;
    }

    public User register(RegisterRequest request) {
        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setMobileNumber(request.getMobileNumber());

            // Validate and set role
            try {
                user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role. Allowed values are: ADMIN, ORGANIZER, CANDIDATE");
            }

            user.setStatus(User.UserStatus.ACTIVE);
            return userService.registerUser(user);
        } catch (RuntimeException e) {
            // Re-throw to be handled by GlobalExceptionHandler
            throw e;
        }
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        Optional<User> userOpt = userService.findByEmail(request.getEmail());

        // Always return success to prevent email enumeration
        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();

        // Generate reset token

        LocalDateTime expiry = LocalDateTime.now().plusHours(24); // Token valid for 24 hours

        // Save token to separate table
        PasswordResetToken passwordResetToken = userService.saveResetToken(user.getId(), expiry);

        // Send email with reset link
        emailService.sendPasswordResetEmail(user.getEmail(), passwordResetToken.getToken());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Find reset token
        Optional<PasswordResetToken> tokenOpt = userService.findByResetToken(request.getToken());

        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Invalid or expired reset token");
        }

        PasswordResetToken resetToken = tokenOpt.get();

        // Check if token is expired
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        // Check if token is already used
        if (resetToken.getUsed()) {
            throw new RuntimeException("Reset token has already been used");
        }

        // Validate new password
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters long");
        }

        // Get user and update password
        User user = userService.getUserById(resetToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.updateUser(user);

        // Mark token as used
        userService.markTokenAsUsed(request.getToken());
    }
}

