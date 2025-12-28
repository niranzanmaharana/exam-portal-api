package com.niranzan.exam.controller;

import com.niranzan.exam.dto.BulkUploadResponse;
import com.niranzan.exam.dto.QuestionRequest;
import com.niranzan.exam.entity.Question;
import com.niranzan.exam.service.ExcelService;
import com.niranzan.exam.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/questions")
@Tag(name = "Questions", description = "Question bank management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ExcelService excelService;

    @Operation(
            summary = "Get all questions",
            description = "Retrieves a list of all questions. Requires authentication."
    )
    @ApiResponse(responseCode = "200", description = "List of questions retrieved successfully")
    @GetMapping
    public ResponseEntity<List<Question>> getAllQuestions() {
        return ResponseEntity.ok(questionService.getAllQuestions());
    }

    @Operation(
            summary = "Get question by ID",
            description = "Retrieves a specific question by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Question found",
                    content = @Content(schema = @Schema(implementation = Question.class))),
            @ApiResponse(responseCode = "404", description = "Question not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Question> getQuestionById(
            @Parameter(description = "Question ID", required = true) @PathVariable Long id) {
        Optional<Question> question = questionService.getQuestionById(id);
        return question.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Get questions by exam ID",
            description = "Retrieves all questions associated with a specific exam"
    )
    @ApiResponse(responseCode = "200", description = "List of questions retrieved successfully")
    @GetMapping("/exam/{examId}")
    public ResponseEntity<List<Question>> getQuestionsByExamId(
            @Parameter(description = "Exam ID", required = true) @PathVariable Long examId) {
        return ResponseEntity.ok(questionService.getQuestionsByExamId(examId));
    }

    @Operation(
            summary = "Get question bank questions",
            description = "Retrieves all questions available in the question bank (can be mapped to multiple exams)"
    )
    @ApiResponse(responseCode = "200", description = "List of question bank questions retrieved successfully")
    @GetMapping("/bank")
    public ResponseEntity<List<Question>> getQuestionBankQuestions() {
        return ResponseEntity.ok(questionService.getQuestionBankQuestions());
    }

    @Operation(
            summary = "Get all mappable questions",
            description = "Retrieves all questions that can be mapped to exams (question bank + all questions)"
    )
    @ApiResponse(responseCode = "200", description = "List of mappable questions retrieved successfully")
    @GetMapping("/mappable")
    public ResponseEntity<List<Question>> getAllMappableQuestions() {
        return ResponseEntity.ok(questionService.getAllMappableQuestions());
    }

    @Operation(
            summary = "Get random questions for exam-taking (Public)",
            description = "Retrieves random questions for an exam based on maxQuestions setting. No authentication required."
    )
    @ApiResponse(responseCode = "200", description = "List of questions retrieved successfully")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "")
    @GetMapping("/public/exam/{examId}/questions")
    public ResponseEntity<List<Question>> getRandomQuestionsForExam(
            @Parameter(description = "Exam ID", required = true) @PathVariable Long examId) {
        return ResponseEntity.ok(questionService.getRandomQuestionsForExam(examId));
    }

    @Operation(
            summary = "Create a new question",
            description = "Creates a new question. Requires ORGANIZER or ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Question created successfully",
            content = @Content(schema = @Schema(implementation = Question.class)))
    @PostMapping
    public ResponseEntity<Question> createQuestion(@RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionService.createQuestion(request));
    }

    @Operation(
            summary = "Update an existing question",
            description = "Updates an existing question by ID. Requires ORGANIZER or ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Question updated successfully",
            content = @Content(schema = @Schema(implementation = Question.class)))
    @PutMapping("/{id}")
    public ResponseEntity<Question> updateQuestion(
            @Parameter(description = "Question ID", required = true) @PathVariable Long id,
            @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionService.updateQuestion(id, request));
    }

    @Operation(
            summary = "Delete a question",
            description = "Deletes a question by ID. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "204", description = "Question deleted successfully")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @Parameter(description = "Question ID", required = true) @PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Download question template Excel file",
            description = "Downloads an Excel template file that can be filled with questions and uploaded for bulk creation."
    )
    @ApiResponse(responseCode = "200", description = "Template file downloaded successfully")
    @GetMapping("/template/download")
    public ResponseEntity<byte[]> downloadQuestionTemplate() {
        try {
            byte[] templateBytes = excelService.generateQuestionTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "question_template.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(templateBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Upload questions from Excel file",
            description = "Uploads an Excel file containing questions and creates them in bulk. Returns a summary of successful and failed imports."
    )
    @ApiResponse(responseCode = "200", description = "Questions uploaded successfully",
            content = @Content(schema = @Schema(implementation = BulkUploadResponse.class)))
    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkUploadResponse> bulkUploadQuestions(
            @Parameter(description = "Excel file containing questions", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                BulkUploadResponse errorResponse = new BulkUploadResponse();
                errorResponse.getErrors().add(new BulkUploadResponse.ErrorDetail(
                    0, "", "File is empty"
                ));
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Validate file type
            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
                BulkUploadResponse errorResponse = new BulkUploadResponse();
                errorResponse.getErrors().add(new BulkUploadResponse.ErrorDetail(
                    0, "", "Invalid file type. Please upload an Excel file (.xlsx or .xls)"
                ));
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Parse Excel file
            List<ExcelService.QuestionExcelRow> excelRows = excelService.parseQuestionFile(file);

            // Bulk create questions
            BulkUploadResponse response = questionService.bulkCreateQuestions(excelRows);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            BulkUploadResponse errorResponse = new BulkUploadResponse();
            errorResponse.getErrors().add(new BulkUploadResponse.ErrorDetail(
                0, "", "Error reading file: " + e.getMessage()
            ));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            BulkUploadResponse errorResponse = new BulkUploadResponse();
            errorResponse.getErrors().add(new BulkUploadResponse.ErrorDetail(
                0, "", "Unexpected error: " + e.getMessage()
            ));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
