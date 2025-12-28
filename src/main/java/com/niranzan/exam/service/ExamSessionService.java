package com.niranzan.exam.service;

import com.niranzan.exam.dto.ExamSessionRequest;
import com.niranzan.exam.dto.ExceptionReportRequest;
import com.niranzan.exam.entity.ExamSession;
import com.niranzan.exam.entity.User;
import com.niranzan.exam.repository.ExamRepository;
import com.niranzan.exam.repository.ExamSessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class ExamSessionService {

    @Autowired
    private ExamSessionRepository examSessionRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public ExamSession createSession(ExamSessionRequest request) {
        // Check if exam exists
        if (!examRepository.existsById(request.getExamId())) {
            throw new RuntimeException("Exam not found");
        }

        ExamSession session = new ExamSession();
        session.setExamId(request.getExamId());

        // Prioritize studentId if provided (authenticated users)
        if (request.getStudentId() != null) {
            // Use authenticated user's ID and populate student info from user
            session.setStudentId(request.getStudentId());
            Optional<User> studentOpt = userService.getUserById(request.getStudentId());
            if (studentOpt.isPresent()) {
                User student = studentOpt.get();
                String fullName = extractFullName(student);
                session.setStudentName(fullName);
                session.setRegistrationNumber(student.getRegistrationCode() != null ?
                        student.getRegistrationCode() : student.getEmail());
            } else {
                throw new RuntimeException("Student not found with ID: " + request.getStudentId());
            }
        } else if (request.getStudentName() != null && request.getRegistrationNumber() != null) {
            // For backward compatibility: find student by registration code (public access)
            try {
                User student = userService.findStudentByRegistrationCode(
                        request.getRegistrationNumber()
                );
                session.setStudentId(student.getId());
                session.setStudentName(request.getStudentName());
                session.setRegistrationNumber(request.getRegistrationNumber());
            } catch (RuntimeException e) {
                throw new RuntimeException("Invalid registration code: " + request.getRegistrationNumber());
            }
        } else {
            throw new RuntimeException("Either studentId or studentName with registrationNumber must be provided");
        }

        session.setStartTime(LocalDateTime.now());
        session.setStatus(ExamSession.SessionStatus.IN_PROGRESS);

        return examSessionRepository.save(session);
    }

    private static String extractFullName(User student) {
        String fullName = "";
        if (student.getFirstName() != null && !student.getFirstName().isEmpty()) {
            fullName = student.getFirstName();
        }
        if (student.getLastName() != null && !student.getLastName().isEmpty()) {
            if (!fullName.isEmpty()) {
                fullName += " " + student.getLastName();
            } else {
                fullName = student.getLastName();
            }
        }
        if (fullName.isEmpty()) {
            fullName = student.getUsername();
        }
        return fullName;
    }

    @Transactional
    public ExamSession submitSession(Long sessionId) {
        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // If exception was already reported, keep status as TERMINATED
        // Otherwise, set status to SUBMITTED
        if (session.getExceptionReason() == null || session.getExceptionReason().isEmpty()) {
            session.setStatus(ExamSession.SessionStatus.SUBMITTED);
            log.info("Session {} submitted normally", sessionId);
        } else {
            // Keep status as TERMINATED if exception was reported
            // This ensures the exception reason is preserved
            if (session.getStatus() != ExamSession.SessionStatus.TERMINATED) {
                session.setStatus(ExamSession.SessionStatus.TERMINATED);
            }
            log.info("Session {} submitted with exception: {}", sessionId, session.getExceptionReason());
        }

        session.setSubmittedAt(LocalDateTime.now());
        session.setEndTime(LocalDateTime.now());

        return examSessionRepository.save(session);
    }

    @Transactional
    public ExamSession reportException(ExceptionReportRequest request) {
        ExamSession session = examSessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setStatus(ExamSession.SessionStatus.TERMINATED);
        session.setExceptionReason(request.getReason());
        session.setEndTime(LocalDateTime.now());

        log.info("Exception reported for session {}: {}", request.getSessionId(), request.getReason());
        return examSessionRepository.save(session);
    }

    public Optional<ExamSession> getSessionById(Long sessionId) {
        return examSessionRepository.findById(sessionId);
    }
}

