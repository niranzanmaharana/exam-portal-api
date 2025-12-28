package com.niranzan.exam.controller;

import com.niranzan.exam.dto.ExamRequest;
import com.niranzan.exam.entity.Exam;
import com.niranzan.exam.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/exams")
@Tag(name = "Exams", description = "Exam management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ExamController {

    @Autowired
    private ExamService examService;

    @Operation(
            summary = "Get all exams",
            description = "Retrieves a list of all exams. Admin sees all, organizers see their own. Requires authentication."
    )
    @ApiResponse(responseCode = "200", description = "List of exams retrieved successfully")
    @GetMapping
    public ResponseEntity<List<Exam>> getExamsForCurrentUser() {
        return ResponseEntity.ok(examService.getExamsForCurrentUser());
    }

    @Operation(
            summary = "Get all exams (Admin only)",
            description = "Retrieves a list of all exams. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "List of all exams retrieved successfully")
    @GetMapping("/all")
    public ResponseEntity<List<Exam>> getAllExams() {
        return ResponseEntity.ok(examService.getAllExams());
    }

    @Operation(
            summary = "Get exam by ID",
            description = "Retrieves a specific exam by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exam found",
                    content = @Content(schema = @Schema(implementation = Exam.class))),
            @ApiResponse(responseCode = "404", description = "Exam not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Exam> getExamById(
            @Parameter(description = "Exam ID", required = true) @PathVariable Long id) {
        Optional<Exam> exam = examService.getExamById(id);
        return exam.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Create a new exam",
            description = "Creates a new exam. Requires ORGANIZER or ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Exam created successfully",
            content = @Content(schema = @Schema(implementation = Exam.class)))
    @PostMapping
    public ResponseEntity<Exam> createExam(@RequestBody ExamRequest request) {
        return ResponseEntity.ok(examService.createExam(request));
    }

    @Operation(
            summary = "Update an existing exam",
            description = "Updates an existing exam by ID. Only creator or admin can update."
    )
    @ApiResponse(responseCode = "200", description = "Exam updated successfully",
            content = @Content(schema = @Schema(implementation = Exam.class)))
    @PutMapping("/{id}")
    public ResponseEntity<Exam> updateExam(
            @Parameter(description = "Exam ID", required = true) @PathVariable Long id,
            @RequestBody ExamRequest request) {
        return ResponseEntity.ok(examService.updateExam(id, request));
    }

    @Operation(
            summary = "Delete an exam",
            description = "Deletes an exam by ID. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "204", description = "Exam deleted successfully")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExam(
            @Parameter(description = "Exam ID", required = true) @PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get available exams",
            description = "Retrieves all published exams available for candidates to take"
    )
    @ApiResponse(responseCode = "200", description = "List of available exams retrieved successfully")
    @GetMapping("/available")
    public ResponseEntity<List<Exam>> getAvailableExams() {
        return ResponseEntity.ok(examService.getExamsByStatus(Exam.ExamStatus.PUBLISHED));
    }

    @Operation(
            summary = "Publish an exam",
            description = "Changes exam status from DRAFT to PUBLISHED. Only creator or admin can publish."
    )
    @ApiResponse(responseCode = "200", description = "Exam published successfully",
            content = @Content(schema = @Schema(implementation = Exam.class)))
    @PostMapping("/{id}/publish")
    public ResponseEntity<Exam> publishExam(
            @Parameter(description = "Exam ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(examService.publishExam(id));
    }

    @Operation(
            summary = "Get exam by access code (Public)",
            description = "Retrieves exam details using access code. No authentication required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exam found",
                    content = @Content(schema = @Schema(implementation = Exam.class))),
            @ApiResponse(responseCode = "404", description = "Exam not found or invalid access code")
    })
    @GetMapping("/access/{accessCode}")
    public ResponseEntity<Exam> getExamByAccessCode(
            @Parameter(description = "Exam access code", required = true) @PathVariable String accessCode) {
        Optional<Exam> exam = examService.getExamByAccessCode(accessCode);
        return exam.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Regenerate access code",
            description = "Generates a new access code for a published exam. Only creator or admin can regenerate."
    )
    @ApiResponse(responseCode = "200", description = "Access code regenerated successfully",
            content = @Content(schema = @Schema(implementation = Exam.class)))
    @PostMapping("/{id}/regenerate-access-code")
    public ResponseEntity<Exam> regenerateAccessCode(
            @Parameter(description = "Exam ID", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(examService.regenerateAccessCode(id));
    }

    @Operation(
            summary = "Start exam with public access (No Auth Required)",
            description = "Starts an exam session using access code, student name and registration number. No authentication required."
    )
    @ApiResponse(responseCode = "200", description = "Exam session started successfully")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "")
    @PostMapping("/public/start")
    public ResponseEntity<?> startExamWithPublicAccess(
            @RequestBody com.niranzan.exam.dto.PublicExamStartRequest request) {
        return ResponseEntity.ok(examService.startExamWithPublicAccess(request));
    }

    @Operation(
            summary = "Add questions to exam",
            description = "Maps existing questions to an exam. Only DRAFT exams can be modified. Requires ORGANIZER role."
    )
    @ApiResponse(responseCode = "200", description = "Questions added successfully",
            content = @Content(schema = @Schema(implementation = Exam.class)))
    @PostMapping("/{id}/questions")
    public ResponseEntity<Exam> addQuestionsToExam(
            @Parameter(description = "Exam ID", required = true) @PathVariable Long id,
            @RequestBody java.util.List<Long> questionIds) {
        return ResponseEntity.ok(examService.addQuestionsToExam(id, questionIds));
    }

    @Operation(
            summary = "Remove questions from exam",
            description = "Removes questions from an exam. Only DRAFT exams can be modified. Requires ORGANIZER role."
    )
    @ApiResponse(responseCode = "200", description = "Questions removed successfully",
            content = @Content(schema = @Schema(implementation = Exam.class)))
    @DeleteMapping("/{id}/questions")
    public ResponseEntity<Exam> removeQuestionsFromExam(
            @Parameter(description = "Exam ID", required = true) @PathVariable Long id,
            @RequestBody java.util.List<Long> questionIds) {
        return ResponseEntity.ok(examService.removeQuestionsFromExam(id, questionIds));
    }

    @Operation(
            summary = "Set questions for exam",
            description = "Replaces all questions for an exam with the provided list. Only DRAFT exams can be modified. Requires ORGANIZER role."
    )
    @ApiResponse(responseCode = "200", description = "Questions set successfully",
            content = @Content(schema = @Schema(implementation = Exam.class)))
    @PutMapping("/{id}/questions")
    public ResponseEntity<Exam> setQuestionsForExam(
            @Parameter(description = "Exam ID", required = true) @PathVariable Long id,
            @RequestBody java.util.List<Long> questionIds) {
        return ResponseEntity.ok(examService.setQuestionsForExam(id, questionIds));
    }
}
