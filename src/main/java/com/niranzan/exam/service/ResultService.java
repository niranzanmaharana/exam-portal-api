package com.niranzan.exam.service;

import com.niranzan.exam.dto.AnswerWithQuestionDto;
import com.niranzan.exam.entity.*;
import com.niranzan.exam.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ResultService {

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private ExamSessionRepository examSessionRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Transactional
    public Result calculateAndSaveResult(Long sessionId, Map<Long, String> answers) {
        ExamSession session = examSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Exam exam = examRepository.findById(session.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        // Get all questions for this exam from many-to-many relationship
        List<Question> allQuestions = examRepository.findQuestionsByExamId(exam.getId());
        
        // Use exam's total marks if available, otherwise calculate from all questions
        int totalMarks = exam.getTotalMarks() != null ? exam.getTotalMarks() : 0;
        if (totalMarks == 0) {
            // Calculate total marks from all questions
            for (Question question : allQuestions) {
                totalMarks += question.getMarks();
            }
        }
        
        int obtainedMarks = 0;

        // Process all questions - save answers for both answered and unanswered questions
        for (Question question : allQuestions) {
            Long questionId = question.getId();
            String answerText = answers != null ? answers.get(questionId) : null;
            
            // If no answer provided, use empty string
            if (answerText == null) {
                answerText = "";
            }

            // Check if answer is correct
            boolean isCorrect = isAnswerCorrect(question, answerText);
            int marksObtained = isCorrect ? question.getMarks() : 0;
            obtainedMarks += marksObtained;

            // Save answer (even if empty/unanswered)
            Answer answer = new Answer();
            answer.setSessionId(sessionId);
            answer.setQuestionId(questionId);
            answer.setAnswerText(answerText);
            answer.setIsCorrect(isCorrect);
            answer.setMarksObtained(marksObtained);
            answerRepository.save(answer);
        }

        // Calculate percentage
        double percentage = totalMarks > 0 ? (obtainedMarks * 100.0) / totalMarks : 0.0;

        // Determine grade (passing marks is in marks, not percentage)
        String grade = calculateGrade(percentage, obtainedMarks, exam.getPassingMarks());

        // Create result
        Result result = new Result();
        result.setSessionId(sessionId);
        result.setExamId(exam.getId());
        result.setStudentId(session.getStudentId());
        result.setStudentName(session.getStudentName());
        result.setRegistrationNumber(session.getRegistrationNumber());
        result.setTotalMarks(totalMarks);
        result.setObtainedMarks(obtainedMarks);
        result.setPercentage(percentage);
        result.setGrade(grade);

        return resultRepository.save(result);
    }

    private boolean isAnswerCorrect(Question question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return false;
        }

        String correctAnswer = question.getCorrectAnswer();
        if (correctAnswer == null) {
            return false;
        }

        // Normalize answers for comparison (case-insensitive, trim)
        String normalizedUserAnswer = userAnswer.trim().toLowerCase();
        String normalizedCorrectAnswer = correctAnswer.trim().toLowerCase();

        // For multiple choice and true/false, exact match
        if (question.getQuestionType().equals("MULTIPLE_CHOICE") || 
            question.getQuestionType().equals("TRUE_FALSE") ||
            question.getQuestionType().equals("BOOLEAN")) {
            return normalizedUserAnswer.equals(normalizedCorrectAnswer);
        }

        // For short answer and essay, partial match or exact match
        return normalizedUserAnswer.equals(normalizedCorrectAnswer) ||
               normalizedUserAnswer.contains(normalizedCorrectAnswer) ||
               normalizedCorrectAnswer.contains(normalizedUserAnswer);
    }

    private String calculateGrade(double percentage, int obtainedMarks, Integer passingMarks) {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B+";
        if (percentage >= 60) return "B";
        // Check if student passed (obtained marks >= passing marks)
        if (obtainedMarks >= passingMarks) return "C";
        return "F";
    }

    public Optional<Result> getResultBySessionId(Long sessionId) {
        return resultRepository.findBySessionId(sessionId);
    }

    public List<Result> getResultsByExamId(Long examId) {
        return resultRepository.findByExamId(examId);
    }

    public List<Result> getResultsByStudentId(Long studentId) {
        return resultRepository.findByStudentId(studentId);
    }

    public List<Result> getAllResults() {
        return resultRepository.findAll();
    }

    public Optional<Result> getResultById(Long id) {
        return resultRepository.findById(id);
    }

    public List<AnswerWithQuestionDto> getAnswersWithQuestions(Long sessionId) {
        List<Answer> answers = answerRepository.findBySessionId(sessionId);
        
        return answers.stream()
                .filter(answer -> {
                    // Only include answers that were actually attempted (non-empty answer text)
                    return answer.getAnswerText() != null && 
                           !answer.getAnswerText().trim().isEmpty();
                })
                .map(answer -> {
            Question question = questionRepository.findById(answer.getQuestionId())
                    .orElse(null);
            
            if (question == null) {
                return null;
            }
            
            AnswerWithQuestionDto dto = new AnswerWithQuestionDto();
            dto.setAnswerId(answer.getId());
            dto.setQuestionId(question.getId());
            dto.setQuestionText(question.getQuestionText());
            dto.setQuestionType(question.getQuestionType() != null ? question.getQuestionType().name() : null);
            dto.setOptions(question.getOptions());
            dto.setCorrectAnswer(question.getCorrectAnswer());
            dto.setQuestionMarks(question.getMarks());
            dto.setDifficulty(question.getDifficulty() != null ? question.getDifficulty().name() : null);
            dto.setCategoryName(question.getCategory() != null ? question.getCategory().getName() : null);
            
            dto.setAnswerText(answer.getAnswerText());
            dto.setIsCorrect(answer.getIsCorrect());
            dto.setMarksObtained(answer.getMarksObtained());
            dto.setAnsweredAt(answer.getCreatedAt());
            
            return dto;
        }).filter(dto -> dto != null).collect(Collectors.toList());
    }
}

