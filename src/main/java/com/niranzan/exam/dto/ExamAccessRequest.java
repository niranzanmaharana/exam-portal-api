package com.niranzan.exam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request to access exam using access code")
public class ExamAccessRequest {
    
    @Schema(description = "Student name", requiredMode = Schema.RequiredMode.REQUIRED, example = "John Doe")
    private String studentName;
    
    @Schema(description = "Student registration number", requiredMode = Schema.RequiredMode.REQUIRED, example = "REG123456")
    private String registrationNumber;
}

