package com.niranzan.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "User registration request")
public class RegisterRequest {

    @Schema(description = "Unique username", example = "john_doe")
    private String username;

    @Schema(description = "User email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User password", example = "SecurePassword123!")
    private String password;

    @Schema(description = "First name", example = "John")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "Mobile number", example = "+1234567890")
    private String mobileNumber;

    @Schema(description = "User role", example = "CANDIDATE",
            allowableValues = {"ADMIN", "ORGANIZER", "CANDIDATE"})
    private String role;
}
