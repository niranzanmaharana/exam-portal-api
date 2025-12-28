package com.niranzan.exam.dto;

import com.niranzan.exam.entity.Exam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Exam creation/update request")
public class ExamRequest {
    
    @Schema(description = "Exam title", requiredMode = Schema.RequiredMode.REQUIRED, example = "Mathematics Final Exam")
    private String title;
    
    @Schema(description = "Exam description", example = "Comprehensive mathematics examination")
    private String description;
    
    @Schema(description = "Duration in minutes", requiredMode = Schema.RequiredMode.REQUIRED, example = "120")
    private Integer duration;
    
    @Schema(description = "Total marks", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer totalMarks;
    
    @Schema(description = "Passing marks", requiredMode = Schema.RequiredMode.REQUIRED, example = "40")
    private Integer passingMarks;
    
    @Schema(description = "Start time", example = "2024-01-01T10:00:00")
    private LocalDateTime startTime;
    
    @Schema(description = "End time", example = "2024-01-01T12:00:00")
    private LocalDateTime endTime;
    
    @Schema(description = "Rules and restrictions for this exam (supports HTML)", example = "No calculators allowed. Mobile phones must be switched off.")
    private String rulesAndRestrictions;
    
    @Schema(description = "Maximum number of questions to show (null = show all questions)", example = "10")
    private Integer maxQuestions;
    
    @Schema(description = "Require fullscreen mode during exam", example = "true")
    private Boolean requireFullscreen;
    
    @Schema(description = "Disable right-click during exam", example = "true")
    private Boolean disableRightClick;
    
    @Schema(description = "Require camera access during exam", example = "false")
    private Boolean requireCamera;
    
    @Schema(description = "Disable copy/paste during exam", example = "true")
    private Boolean disableCopyPaste;
    
    @Schema(description = "Disable print screen during exam", example = "true")
    private Boolean disablePrintScreen;
    
    @Schema(description = "Prevent switching browser tabs and window focus loss", example = "true")
    private Boolean preventTabSwitch;
    
    @Schema(description = "Exam status", example = "DRAFT")
    private Exam.ExamStatus status;
}

