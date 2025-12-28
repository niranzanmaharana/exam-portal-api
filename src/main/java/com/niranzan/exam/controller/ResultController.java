package com.niranzan.exam.controller;

import com.niranzan.exam.dto.ResultWithExamDTO;
import com.niranzan.exam.entity.Result;
import com.niranzan.exam.entity.User;
import com.niranzan.exam.repository.ExamRepository;
import com.niranzan.exam.repository.ExamSessionRepository;
import com.niranzan.exam.repository.UserRepository;
import com.niranzan.exam.service.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/results")
@Tag(name = "Results", description = "Exam results endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ResultController {

    @Autowired
    private ResultService resultService;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamSessionRepository examSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Get results for current user", description = "Returns results for the authenticated user. Candidates see only their own results, Admin/Organizer see all results.")
    @GetMapping
    public ResponseEntity<List<ResultWithExamDTO>> getResults() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Result> results;
        if (currentUser.getRole() == User.Role.CANDIDATE) {
            // Candidates see only their own results
            results = resultService.getResultsByStudentId(currentUser.getId());
        } else {
            // Admin and Organizer see all results
            results = resultService.getAllResults();
        }

        List<ResultWithExamDTO> resultDTOs = results.stream()
                .map(result -> {
                    ResultWithExamDTO dto = new ResultWithExamDTO();
                    dto.setId(result.getId());
                    dto.setSessionId(result.getSessionId());
                    dto.setExamId(result.getExamId());
                    dto.setStudentId(result.getStudentId());
                    dto.setStudentName(result.getStudentName());
                    dto.setRegistrationNumber(result.getRegistrationNumber());
                    dto.setTotalMarks(result.getTotalMarks());
                    dto.setObtainedMarks(result.getObtainedMarks());
                    dto.setPercentage(result.getPercentage());
                    dto.setGrade(result.getGrade());
                    dto.setCreatedAt(result.getCreatedAt());

                    // Get exam details
                    examRepository.findById(result.getExamId()).ifPresent(exam -> {
                        dto.setExamTitle(exam.getTitle());
                        dto.setExamDescription(exam.getDescription());
                        dto.setPassingMarks(exam.getPassingMarks());
                    });

                    // Get session details for exam date
                    examSessionRepository.findById(result.getSessionId()).ifPresent(session -> {
                        dto.setExamDate(session.getStartTime());
                        dto.setSubmittedAt(session.getSubmittedAt());
                        dto.setStatus(session.getStatus().name());
                        dto.setExceptionReason(session.getExceptionReason());
                    });

                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(resultDTOs);
    }

    @Operation(summary = "Get result by ID", description = "Retrieves a specific result by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<ResultWithExamDTO> getResultById(@PathVariable Long id) {
        Result result = resultService.getResultById(id)
                .orElseThrow(() -> new RuntimeException("Result not found"));

        ResultWithExamDTO dto = new ResultWithExamDTO();
        dto.setId(result.getId());
        dto.setSessionId(result.getSessionId());
        dto.setExamId(result.getExamId());
        dto.setStudentId(result.getStudentId());
        dto.setStudentName(result.getStudentName());
        dto.setRegistrationNumber(result.getRegistrationNumber());
        dto.setTotalMarks(result.getTotalMarks());
        dto.setObtainedMarks(result.getObtainedMarks());
        dto.setPercentage(result.getPercentage());
        dto.setGrade(result.getGrade());
        dto.setCreatedAt(result.getCreatedAt());

        // Get exam details
        examRepository.findById(result.getExamId()).ifPresent(exam -> {
            dto.setExamTitle(exam.getTitle());
            dto.setExamDescription(exam.getDescription());
            dto.setPassingMarks(exam.getPassingMarks());
        });

        // Get session details
        examSessionRepository.findById(result.getSessionId()).ifPresent(session -> {
            dto.setExamDate(session.getStartTime());
            dto.setSubmittedAt(session.getSubmittedAt());
            dto.setStatus(session.getStatus().name());
            dto.setExceptionReason(session.getExceptionReason());
        });

        return ResponseEntity.ok(dto);
    }
}

