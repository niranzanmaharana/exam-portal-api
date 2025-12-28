package com.niranzan.exam.controller;

import com.niranzan.exam.dto.AnswerSubmissionRequest;
import com.niranzan.exam.dto.AnswerWithQuestionDto;
import com.niranzan.exam.dto.ExamSessionRequest;
import com.niranzan.exam.dto.ExceptionReportRequest;
import com.niranzan.exam.entity.ExamSession;
import com.niranzan.exam.entity.Result;
import com.niranzan.exam.service.ExamSessionService;
import com.niranzan.exam.service.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/exam-sessions")
@Tag(name = "Exam Sessions", description = "Exam session management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ExamSessionController {

    @Autowired
    private ExamSessionService examSessionService;

    @Autowired
    private ResultService resultService;

    @Operation(summary = "Create exam session", description = "Creates a new exam session when student starts the exam")
    @PostMapping("/create")
    public ResponseEntity<?> createSession(@RequestBody ExamSessionRequest request) {
        try {
            ExamSession session = examSessionService.createSession(request);
            return ResponseEntity.ok(session);
        } catch (RuntimeException e) {
            log.error("Error creating exam session: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error creating exam session: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to create exam session"));
        }
    }

    @Operation(summary = "Submit exam", description = "Submits the exam and calculates results")
    @PostMapping("/submit")
    public ResponseEntity<Result> submitExam(@RequestBody AnswerSubmissionRequest request) {
        try {
            // Submit session
            ExamSession examSession = examSessionService.submitSession(request.getSessionId());
            log.info("Exam session submitted with id: {}", examSession.getId());
            // Calculate and save results (answers can be empty/null for terminated exams)
            Map<Long, String> answers = request.getAnswers() != null ? request.getAnswers() : new java.util.HashMap<>();
            Result result = resultService.calculateAndSaveResult(request.getSessionId(), answers);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            // Log the error for debugging
            log.error("Error submitting exam: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            // Log unexpected errors
            log.error("Unexpected error submitting exam: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Report exception", description = "Reports an exception/violation during exam")
    @PostMapping("/exception")
    public ResponseEntity<ExamSession> reportException(@RequestBody ExceptionReportRequest request) {
        try {
            ExamSession session = examSessionService.reportException(request);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get session by ID", description = "Retrieves exam session details")
    @GetMapping("/{sessionId}")
    public ResponseEntity<ExamSession> getSession(@PathVariable Long sessionId) {
        return examSessionService.getSessionById(sessionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get result by session ID", description = "Retrieves exam result for a session")
    @GetMapping("/{sessionId}/result")
    public ResponseEntity<Result> getResult(@PathVariable Long sessionId) {
        return resultService.getResultBySessionId(sessionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get answers with questions by session ID", description = "Retrieves all answers with question details for a session")
    @GetMapping("/{sessionId}/answers")
    public ResponseEntity<List<AnswerWithQuestionDto>> getAnswersWithQuestions(@PathVariable Long sessionId) {
        List<AnswerWithQuestionDto> answers = resultService.getAnswersWithQuestions(sessionId);
        return ResponseEntity.ok(answers);
    }
}

