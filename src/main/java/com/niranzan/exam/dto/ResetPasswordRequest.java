package com.niranzan.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Reset password request")
public class ResetPasswordRequest {
    
    @Schema(description = "Password reset token", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;
    
    @Schema(description = "New password", requiredMode = Schema.RequiredMode.REQUIRED, example = "NewPassword123!")
    private String newPassword;
}

