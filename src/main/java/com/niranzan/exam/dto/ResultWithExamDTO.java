package com.niranzan.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultWithExamDTO {
    private Long id;
    private Long sessionId;
    private Long examId;
    private Long studentId;
    private String studentName;
    private String registrationNumber;
    private Integer totalMarks;
    private Integer obtainedMarks;
    private Double percentage;
    private String grade;
    private LocalDateTime createdAt;

    // Exam details
    private String examTitle;
    private String examDescription;
    private Integer passingMarks;

    // Session details
    private LocalDateTime examDate; // Exam taken date (from session startTime)
    private LocalDateTime submittedAt;
    private String status;
    private String exceptionReason;
}

