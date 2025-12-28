package com.niranzan.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamSessionRequest {
    private Long examId;
    private Long studentId; // Nullable for authenticated users
    private String studentName; // For public exam access
    private String registrationNumber; // For public exam access
}

