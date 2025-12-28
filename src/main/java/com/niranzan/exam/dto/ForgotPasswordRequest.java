package com.niranzan.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Forgot password request")
public class ForgotPasswordRequest {
    
    @Schema(description = "User email address", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
    private String email;
}

