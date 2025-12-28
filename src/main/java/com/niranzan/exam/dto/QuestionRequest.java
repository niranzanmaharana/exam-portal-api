package com.niranzan.exam.dto;

import com.niranzan.exam.entity.Question;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Question creation/update request")
public class QuestionRequest {
    
    @Schema(description = "Question text", requiredMode = Schema.RequiredMode.REQUIRED)
    private String questionText;
    
    @Schema(description = "Question type", requiredMode = Schema.RequiredMode.REQUIRED)
    private Question.QuestionType questionType;
    
    @Schema(description = "Options (JSON string for multiple choice)")
    private String options;
    
    @Schema(description = "Correct answer", requiredMode = Schema.RequiredMode.REQUIRED)
    private String correctAnswer;
    
    @Schema(description = "Marks", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer marks;
    
    @Schema(description = "Difficulty level")
    private Question.DifficultyLevel difficulty;
    
    @Schema(description = "Category ID")
    private Long categoryId;
}

