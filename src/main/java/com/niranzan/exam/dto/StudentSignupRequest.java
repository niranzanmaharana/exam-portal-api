package com.niranzan.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentSignupRequest {
    private String studentName;
    private String registrationNumber;
}

