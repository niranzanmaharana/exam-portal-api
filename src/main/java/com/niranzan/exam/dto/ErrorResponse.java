package com.niranzan.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response structure")
public class ErrorResponse {
    
    @Schema(description = "Error type or code", example = "Invalid data format")
    private String error;
    
    @Schema(description = "Detailed error message", example = "Invalid data format. Please check your input values.")
    private String message;
    
    @Schema(description = "Timestamp when the error occurred")
    private LocalDateTime timestamp;
    
    @Schema(description = "HTTP status code", example = "400")
    private int status;
    
    @Schema(description = "Request path where the error occurred", example = "/api/auth/register")
    private String path;

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String error, String message, int status) {
        this.error = error;
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String error, String message, int status, String path) {
        this.error = error;
        this.message = message;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }
}

