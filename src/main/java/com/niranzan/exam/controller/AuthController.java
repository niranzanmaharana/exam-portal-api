package com.niranzan.exam.controller;

import com.niranzan.exam.dto.ForgotPasswordRequest;
import com.niranzan.exam.dto.LoginRequest;
import com.niranzan.exam.dto.LoginResponse;
import com.niranzan.exam.dto.RegisterRequest;
import com.niranzan.exam.dto.ResetPasswordRequest;
import com.niranzan.exam.dto.StudentSignupRequest;
import com.niranzan.exam.entity.User;
import com.niranzan.exam.service.AuthService;
import com.niranzan.exam.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and user registration endpoints")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with the provided information. Roles: ADMIN, ORGANIZER, CANDIDATE"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "User login",
            description = "Authenticates a user and returns a JWT token for subsequent API calls"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Request password reset",
            description = "Sends a password reset email to the user. Always returns success to prevent email enumeration."
    )
    @ApiResponse(responseCode = "200", description = "If email exists, reset link has been sent")
    @PostMapping("/forgot-password")
    public ResponseEntity<Object> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok().body(java.util.Map.of("message", "If the email exists, a password reset link has been sent."));
    }

    @Operation(
            summary = "Reset password",
            description = "Resets the user's password using the reset token from the email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().body(java.util.Map.of("message", "Password has been reset successfully."));
    }

    @Operation(
            summary = "Student signup (Deprecated)",
            description = "DEPRECATED: Students must now register with email and password using the /register endpoint. This endpoint is kept for backward compatibility but will throw an error if student doesn't exist."
    )
    @ApiResponse(responseCode = "200", description = "Student account found",
            content = @Content(schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "400", description = "Student not found - must register first")
    @PostMapping("/student-signup")
    @Deprecated
    public ResponseEntity<?> studentSignup(@RequestBody StudentSignupRequest request) {
        try {
            // Try to find existing student by registration code
            User student = userService.findStudentByRegistrationCode(request.getRegistrationNumber());
            return ResponseEntity.ok(student);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "error", "Student not found",
                "message", "Please register with email and password first using the /register endpoint with role CANDIDATE"
            ));
        }
    }
}
