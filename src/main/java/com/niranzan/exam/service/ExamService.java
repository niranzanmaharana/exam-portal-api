package com.niranzan.exam.service;

import com.niranzan.exam.dto.ExamRequest;
import com.niranzan.exam.entity.Exam;
import com.niranzan.exam.entity.Question;
import com.niranzan.exam.entity.User;
import com.niranzan.exam.repository.ExamRepository;
import com.niranzan.exam.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ExamService {

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private QuestionRepository questionRepository;

    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    public List<Exam> getExamsForCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole() == User.Role.ADMIN) {
            return examRepository.findAll();
        } else if (currentUser.getRole() == User.Role.ORGANIZER) {
            return examRepository.findByCreatedBy(currentUser.getId());
        }

        return List.of();
    }

    public Optional<Exam> getExamById(Long id) {
        return examRepository.findById(id);
    }

    public Exam createExam(ExamRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only ORGANIZER can create exams
        if (currentUser.getRole() != User.Role.ORGANIZER) {
            throw new RuntimeException("Only organizers can create exams. Administrators can only view exams and reports.");
        }

        // Validate passing marks
        if (request.getPassingMarks() > request.getTotalMarks()) {
            throw new RuntimeException("Passing marks cannot be greater than total marks");
        }

        // Validate time range
        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (request.getEndTime().isBefore(request.getStartTime())) {
                throw new RuntimeException("End time cannot be before start time");
            }
        }

        Exam exam = new Exam();
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setDuration(request.getDuration());
        exam.setTotalMarks(request.getTotalMarks());
        exam.setPassingMarks(request.getPassingMarks());
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
        exam.setRulesAndRestrictions(request.getRulesAndRestrictions());
        exam.setMaxQuestions(request.getMaxQuestions());
        exam.setRequireFullscreen(request.getRequireFullscreen() != null ? request.getRequireFullscreen() : false);
        exam.setDisableRightClick(request.getDisableRightClick() != null ? request.getDisableRightClick() : false);
        exam.setRequireCamera(request.getRequireCamera() != null ? request.getRequireCamera() : false);
        exam.setDisableCopyPaste(request.getDisableCopyPaste() != null ? request.getDisableCopyPaste() : false);
        exam.setDisablePrintScreen(request.getDisablePrintScreen() != null ? request.getDisablePrintScreen() : false);
        exam.setPreventTabSwitch(request.getPreventTabSwitch() != null ? request.getPreventTabSwitch() : false);
        exam.setStatus(request.getStatus() != null ? request.getStatus() : Exam.ExamStatus.DRAFT);
        exam.setCreatedBy(currentUser.getId());

        return examRepository.save(exam);
    }

    public Exam updateExam(Long id, ExamRequest request) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only ORGANIZER can update, and only their own exams
        if (currentUser.getRole() != User.Role.ORGANIZER) {
            throw new RuntimeException("Only organizers can update exams. Administrators can only view exams and reports.");
        }
        if (!exam.getCreatedBy().equals(currentUser.getId())) {
            throw new RuntimeException("You don't have permission to update this exam");
        }

        // Validate passing marks
        if (request.getPassingMarks() > request.getTotalMarks()) {
            throw new RuntimeException("Passing marks cannot be greater than total marks");
        }

        // Validate time range
        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (request.getEndTime().isBefore(request.getStartTime())) {
                throw new RuntimeException("End time cannot be before start time");
            }
        }

        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setDuration(request.getDuration());
        exam.setTotalMarks(request.getTotalMarks());
        exam.setPassingMarks(request.getPassingMarks());
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
        exam.setRulesAndRestrictions(request.getRulesAndRestrictions());
        exam.setMaxQuestions(request.getMaxQuestions());
        if (request.getRequireFullscreen() != null) exam.setRequireFullscreen(request.getRequireFullscreen());
        if (request.getDisableRightClick() != null) exam.setDisableRightClick(request.getDisableRightClick());
        if (request.getRequireCamera() != null) exam.setRequireCamera(request.getRequireCamera());
        if (request.getDisableCopyPaste() != null) exam.setDisableCopyPaste(request.getDisableCopyPaste());
        if (request.getDisablePrintScreen() != null) exam.setDisablePrintScreen(request.getDisablePrintScreen());
        if (request.getPreventTabSwitch() != null) exam.setPreventTabSwitch(request.getPreventTabSwitch());

        // Status change rules:
        // - Only Organizer can change status
        // - Organizer can change from DRAFT to PUBLISHED, or keep current status
        if (request.getStatus() != null) {
            if (currentUser.getRole() == User.Role.ORGANIZER) {
                // Organizer can only change from DRAFT to PUBLISHED
                if (exam.getStatus() == Exam.ExamStatus.DRAFT &&
                        request.getStatus() == Exam.ExamStatus.PUBLISHED) {
                    exam.setStatus(Exam.ExamStatus.PUBLISHED);
                }
                // Otherwise, keep the existing status
            }
        }

        return examRepository.save(exam);
    }

    public Exam publishExam(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only ORGANIZER can publish, and only their own exams
        if (currentUser.getRole() != User.Role.ORGANIZER) {
            throw new RuntimeException("Only organizers can publish exams. Administrators can only view exams and reports.");
        }
        if (!exam.getCreatedBy().equals(currentUser.getId())) {
            throw new RuntimeException("You don't have permission to publish this exam");
        }

        // Only DRAFT exams can be published
        if (exam.getStatus() != Exam.ExamStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT exams can be published");
        }

        exam.setStatus(Exam.ExamStatus.PUBLISHED);

        // Generate unique access code if not already set
        if (exam.getAccessCode() == null || exam.getAccessCode().isEmpty()) {
            exam.setAccessCode(generateAccessCode());
        }

        return examRepository.save(exam);
    }

    private String generateAccessCode() {
        // Generate a unique 8-character alphanumeric code
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (examRepository.existsByAccessCode(code));
        return code;
    }

    public Optional<Exam> getExamByAccessCode(String accessCode) {
        return examRepository.findByAccessCode(accessCode);
    }

    public Exam regenerateAccessCode(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only ORGANIZER can regenerate access code, and only their own exams
        if (currentUser.getRole() != User.Role.ORGANIZER) {
            throw new RuntimeException("Only organizers can regenerate access codes. Administrators can only view exams and reports.");
        }
        if (!exam.getCreatedBy().equals(currentUser.getId())) {
            throw new RuntimeException("You don't have permission to regenerate access code for this exam");
        }

        // Only PUBLISHED exams can have access codes regenerated
        if (exam.getStatus() != Exam.ExamStatus.PUBLISHED) {
            throw new RuntimeException("Only PUBLISHED exams can have access codes regenerated");
        }

        // Generate new unique access code
        exam.setAccessCode(generateAccessCode());

        return examRepository.save(exam);
    }

    public void deleteExam(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only creator or admin can delete
        if (!exam.getCreatedBy().equals(currentUser.getId()) &&
                currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("You don't have permission to delete this exam");
        }

        examRepository.deleteById(id);
    }

    public List<Exam> getExamsByCreator(Long createdBy) {
        return examRepository.findByCreatedBy(createdBy);
    }

    public List<Exam> getExamsByStatus(Exam.ExamStatus status) {
        return examRepository.findByStatus(status);
    }

    public Map<String, Object> startExamWithPublicAccess(com.niranzan.exam.dto.PublicExamStartRequest request) {
        // Validate access code
        Exam exam = examRepository.findByAccessCode(request.getAccessCode())
                .orElseThrow(() -> new RuntimeException("Invalid access code"));

        // Check if exam is published
        if (exam.getStatus() != Exam.ExamStatus.PUBLISHED) {
            throw new RuntimeException("This exam is not available for access");
        }

        // Validate student info
        if (request.getStudentName() == null || request.getStudentName().trim().isEmpty()) {
            throw new RuntimeException("Student name is required");
        }

        if (request.getRegistrationNumber() == null || request.getRegistrationNumber().trim().isEmpty()) {
            throw new RuntimeException("Registration number is required");
        }

        // Create exam session (we'll need to inject ExamSessionRepository)
        // For now, return exam info and session data
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("examId", exam.getId());
        response.put("examTitle", exam.getTitle());
        response.put("duration", exam.getDuration());
        response.put("studentName", request.getStudentName().trim());
        response.put("registrationNumber", request.getRegistrationNumber().trim());
        response.put("accessCode", request.getAccessCode());
        response.put("message", "Exam session ready");

        return response;
    }

    /**
     * Add questions to an exam (many-to-many mapping)
     */
    @Transactional
    public Exam addQuestionsToExam(Long examId, List<Long> questionIds) {
        Exam exam = examRepository.findByIdWithQuestions(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only ORGANIZER can add questions, and only to their own exams
        if (currentUser.getRole() != User.Role.ORGANIZER) {
            throw new RuntimeException("Only organizers can add questions to exams");
        }
        if (!exam.getCreatedBy().equals(currentUser.getId())) {
            throw new RuntimeException("You don't have permission to modify this exam");
        }

        // Only DRAFT exams can be modified
        if (exam.getStatus() != Exam.ExamStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT exams can be modified");
        }

        Set<Question> questions = exam.getQuestions();
        if (questions == null) {
            questions = new java.util.HashSet<>();
        }

        for (Long questionId : questionIds) {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new RuntimeException("Question not found: " + questionId));
            questions.add(question);
        }

        exam.setQuestions(questions);
        return examRepository.save(exam);
    }

    /**
     * Remove questions from an exam
     */
    @Transactional
    public Exam removeQuestionsFromExam(Long examId, List<Long> questionIds) {
        Exam exam = examRepository.findByIdWithQuestionsAndCategory(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only ORGANIZER can remove questions, and only from their own exams
        if (currentUser.getRole() != User.Role.ORGANIZER) {
            throw new RuntimeException("Only organizers can remove questions from exams");
        }
        if (!exam.getCreatedBy().equals(currentUser.getId())) {
            throw new RuntimeException("You don't have permission to modify this exam");
        }

        // Only DRAFT exams can be modified
        if (exam.getStatus() != Exam.ExamStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT exams can be modified");
        }

        Set<Question> questions = exam.getQuestions();
        if (questions == null) {
            questions = new java.util.HashSet<>();
        }

        // Remove questions by ID
        for (Long questionId : questionIds) {
            questions.removeIf(q -> q != null && q.getId() != null && q.getId().equals(questionId));
        }

        exam.setQuestions(questions);
        return examRepository.save(exam);
    }

    /**
     * Set questions for an exam (replaces all existing questions)
     */
    @Transactional
    public Exam setQuestionsForExam(Long examId, List<Long> questionIds) {
        Exam exam = examRepository.findByIdWithQuestions(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only ORGANIZER can set questions, and only for their own exams
        if (currentUser.getRole() != User.Role.ORGANIZER) {
            throw new RuntimeException("Only organizers can set questions for exams");
        }
        if (!exam.getCreatedBy().equals(currentUser.getId())) {
            throw new RuntimeException("You don't have permission to modify this exam");
        }

        // Only DRAFT exams can be modified
        if (exam.getStatus() != Exam.ExamStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT exams can be modified");
        }

        Set<Question> questions = new java.util.HashSet<>();
        for (Long questionId : questionIds) {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new RuntimeException("Question not found: " + questionId));
            questions.add(question);
        }

        exam.setQuestions(questions);
        return examRepository.save(exam);
    }
}

