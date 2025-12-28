package com.niranzan.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionReportRequest {
    private Long sessionId;
    private String reason; // e.g., "Tab switch detected", "Window focus lost", "Right click detected", etc.
}

