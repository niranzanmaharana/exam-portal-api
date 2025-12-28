package com.niranzan.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerWithQuestionDto {
    private Long answerId;
    private Long questionId;
    private String questionText;
    private String questionType;
    private String options; // JSON string for multiple choice
    private String correctAnswer;
    private Integer questionMarks;
    private String difficulty;
    private String categoryName;
    
    // Answer details
    private String answerText;
    private Boolean isCorrect;
    private Integer marksObtained;
    private LocalDateTime answeredAt;
}

