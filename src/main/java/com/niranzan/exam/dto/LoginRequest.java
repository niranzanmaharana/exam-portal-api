package com.niranzan.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Login request with username and password")
public class LoginRequest {

    @Schema(description = "Username for login", example = "john_doe")
    private String username;

    @Schema(description = "Password for login", example = "password123")
    private String password;
}
