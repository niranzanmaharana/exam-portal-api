package com.niranzan.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerSubmissionRequest {
    private Long sessionId;
    private Map<Long, String> answers; // questionId -> answer text
}

