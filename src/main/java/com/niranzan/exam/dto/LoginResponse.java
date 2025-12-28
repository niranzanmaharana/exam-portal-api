package com.niranzan.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Login response containing JWT token and user information")
public class LoginResponse {
    
    @Schema(description = "JWT authentication token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "Username", example = "john_doe")
    private String username;
    
    @Schema(description = "User email", example = "john.doe@example.com")
    private String email;
    
    @Schema(description = "User role", example = "CANDIDATE", allowableValues = {"ADMIN", "ORGANIZER", "CANDIDATE"})
    private String role;
    
    @Schema(description = "User ID", example = "1")
    private Long id;
    
    @Schema(description = "Registration code (for CANDIDATE users)", example = "A3B7K")
    private String registrationCode;
    
    @Schema(description = "First name", example = "John")
    private String firstName;
    
    @Schema(description = "Last name", example = "Doe")
    private String lastName;
}
