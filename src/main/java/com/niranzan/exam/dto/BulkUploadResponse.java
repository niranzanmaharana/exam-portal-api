package com.niranzan.exam.dto;

import com.niranzan.exam.entity.Question;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BulkUploadResponse {
    private int totalRows;
    private int successCount;
    private int failureCount;
    private List<Question> createdQuestions = new ArrayList<>();
    private List<ErrorDetail> errors = new ArrayList<>();

    @Data
    public static class ErrorDetail {
        private int rowNumber;
        private String questionText;
        private String errorMessage;

        public ErrorDetail(int rowNumber, String questionText, String errorMessage) {
            this.rowNumber = rowNumber;
            this.questionText = questionText;
            this.errorMessage = errorMessage;
        }
    }
}

