package com.niranzan.exam.service;

import com.niranzan.exam.entity.Question;
import com.niranzan.exam.entity.Category;
import com.niranzan.exam.repository.CategoryRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class ExcelService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Generate Excel template for question upload
     */
    public byte[] generateQuestionTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Questions");

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "Question Text", "Question Type", "Options (JSON)", "Correct Answer", 
            "Marks", "Difficulty", "Category Name"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Create example rows
        createExampleRow(sheet, 1, "What is the capital of France?", 
            "MULTIPLE_CHOICE", 
            "[\"Paris\",\"London\",\"Berlin\",\"Madrid\"]", 
            "Paris", 5, "EASY", "Geography");
        
        createExampleRow(sheet, 2, "Java is a compiled language.", 
            "TRUE_FALSE", 
            "", 
            "True", 2, "EASY", "Programming");
        
        createExampleRow(sheet, 3, "Explain the concept of polymorphism.", 
            "ESSAY", 
            "", 
            "Polymorphism allows objects of different types to be accessed through the same interface.", 
            10, "HARD", "Programming");

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    private void createExampleRow(Sheet sheet, int rowNum, String questionText, 
                                  String questionType, String options, 
                                  String correctAnswer, int marks, 
                                  String difficulty, String categoryName) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(questionText);
        row.createCell(1).setCellValue(questionType);
        row.createCell(2).setCellValue(options);
        row.createCell(3).setCellValue(correctAnswer);
        row.createCell(4).setCellValue(marks);
        row.createCell(5).setCellValue(difficulty);
        row.createCell(6).setCellValue(categoryName);
    }

    /**
     * Parse Excel file and extract questions
     */
    public List<QuestionExcelRow> parseQuestionFile(MultipartFile file) throws IOException {
        List<QuestionExcelRow> questions = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row (row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                QuestionExcelRow questionRow = new QuestionExcelRow();
                questionRow.setRowNumber(i + 1); // 1-based row number for user reference
                
                try {
                    // Question Text (required)
                    Cell questionTextCell = row.getCell(0);
                    if (questionTextCell != null) {
                        questionRow.setQuestionText(getCellValueAsString(questionTextCell));
                    }
                    
                    // Question Type (required)
                    Cell questionTypeCell = row.getCell(1);
                    if (questionTypeCell != null) {
                        questionRow.setQuestionType(getCellValueAsString(questionTypeCell));
                    }
                    
                    // Options (optional, JSON string)
                    Cell optionsCell = row.getCell(2);
                    if (optionsCell != null) {
                        questionRow.setOptions(getCellValueAsString(optionsCell));
                    }
                    
                    // Correct Answer (required)
                    Cell correctAnswerCell = row.getCell(3);
                    if (correctAnswerCell != null) {
                        questionRow.setCorrectAnswer(getCellValueAsString(correctAnswerCell));
                    }
                    
                    // Marks (required)
                    Cell marksCell = row.getCell(4);
                    if (marksCell != null) {
                        if (marksCell.getCellType() == CellType.NUMERIC) {
                            questionRow.setMarks((int) marksCell.getNumericCellValue());
                        } else {
                            String marksStr = getCellValueAsString(marksCell);
                            if (!marksStr.isEmpty()) {
                                questionRow.setMarks(Integer.parseInt(marksStr));
                            }
                        }
                    }
                    
                    // Difficulty (optional)
                    Cell difficultyCell = row.getCell(5);
                    if (difficultyCell != null) {
                        questionRow.setDifficulty(getCellValueAsString(difficultyCell));
                    }
                    
                    // Category Name (optional)
                    Cell categoryCell = row.getCell(6);
                    if (categoryCell != null) {
                        questionRow.setCategoryName(getCellValueAsString(categoryCell));
                    }
                    
                    questions.add(questionRow);
                } catch (Exception e) {
                    questionRow.setError("Error parsing row: " + e.getMessage());
                    questions.add(questionRow);
                }
            }
        }
        
        return questions;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Remove decimal if it's a whole number
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * Convert Excel row to Question entity
     */
    public Question convertToQuestion(QuestionExcelRow excelRow, Long userId) {
        Question question = new Question();
        
        question.setQuestionText(excelRow.getQuestionText());
        
        // Parse question type
        try {
            question.setQuestionType(Question.QuestionType.valueOf(
                excelRow.getQuestionType().toUpperCase().replace(" ", "_")));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid question type: " + excelRow.getQuestionType());
        }
        
        question.setOptions(excelRow.getOptions());
        question.setCorrectAnswer(excelRow.getCorrectAnswer());
        question.setMarks(excelRow.getMarks());
        
        // Parse difficulty
        if (excelRow.getDifficulty() != null && !excelRow.getDifficulty().isEmpty()) {
            try {
                question.setDifficulty(Question.DifficultyLevel.valueOf(
                    excelRow.getDifficulty().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Default to MEDIUM if invalid
                question.setDifficulty(Question.DifficultyLevel.MEDIUM);
            }
        } else {
            question.setDifficulty(Question.DifficultyLevel.MEDIUM);
        }
        
        // Set category if provided - validate that it exists
        if (excelRow.getCategoryName() != null && !excelRow.getCategoryName().isEmpty()) {
            Optional<Category> category = categoryRepository.findByName(excelRow.getCategoryName());
            if (category.isEmpty()) {
                throw new RuntimeException("Category not found: " + excelRow.getCategoryName());
            }
            question.setCategory(category.get());
        }
        
        return question;
    }

    /**
     * Inner class to represent a row from Excel file
     */
    public static class QuestionExcelRow {
        private int rowNumber;
        private String questionText;
        private String questionType;
        private String options;
        private String correctAnswer;
        private Integer marks;
        private String difficulty;
        private String categoryName;
        private String error;

        // Getters and setters
        public int getRowNumber() { return rowNumber; }
        public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }
        
        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }
        
        public String getQuestionType() { return questionType; }
        public void setQuestionType(String questionType) { this.questionType = questionType; }
        
        public String getOptions() { return options; }
        public void setOptions(String options) { this.options = options; }
        
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        
        public Integer getMarks() { return marks; }
        public void setMarks(Integer marks) { this.marks = marks; }
        
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}

