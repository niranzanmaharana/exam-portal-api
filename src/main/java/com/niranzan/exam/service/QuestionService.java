package com.niranzan.exam.service;

import com.niranzan.exam.dto.BulkUploadResponse;
import com.niranzan.exam.dto.QuestionRequest;
import com.niranzan.exam.entity.Category;
import com.niranzan.exam.entity.Exam;
import com.niranzan.exam.entity.Question;
import com.niranzan.exam.repository.CategoryRepository;
import com.niranzan.exam.repository.ExamRepository;
import com.niranzan.exam.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExcelService excelService;

    public List<Question> getAllQuestions() {
        List<Question> questions = questionRepository.findAll();
        // Eagerly load category to avoid lazy loading issues
        questions.forEach(question -> {
            if (question.getCategory() != null) {
                question.getCategory().getId(); // Trigger lazy loading
                question.getCategory().getIsCommon(); // Ensure isCommon is loaded
            }
        });
        return questions;
    }

    public Optional<Question> getQuestionById(Long id) {
        Optional<Question> question = questionRepository.findById(id);
        question.ifPresent(q -> {
            if (q.getCategory() != null) {
                q.getCategory().getId(); // Trigger lazy loading
                q.getCategory().getIsCommon(); // Ensure isCommon is loaded
            }
        });
        return question;
    }

    public Question createQuestion(QuestionRequest request) {
        Question question = new Question();
        question.setQuestionText(request.getQuestionText());
        question.setQuestionType(request.getQuestionType());
        question.setOptions(request.getOptions());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setMarks(request.getMarks());
        question.setDifficulty(request.getDifficulty());

        // Set category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            question.setCategory(category);
        }

        return questionRepository.save(question);
    }

    public Question updateQuestion(Long id, QuestionRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        question.setQuestionText(request.getQuestionText());
        question.setQuestionType(request.getQuestionType());
        question.setOptions(request.getOptions());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setMarks(request.getMarks());
        question.setDifficulty(request.getDifficulty());

        // Set category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            question.setCategory(category);
        } else {
            question.setCategory(null);
        }

        return questionRepository.save(question);
    }

    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }

    public List<Question> getQuestionsByExamId(Long examId) {
        return examRepository.findQuestionsByExamId(examId);
    }

    public List<Question> getRandomQuestionsForExam(Long examId) {
        // Get all questions for the exam from many-to-many relationship
        Optional<Exam> examOpt = examRepository.findById(examId);
        if (examOpt.isEmpty()) {
            throw new RuntimeException("Exam not found");
        }

        Exam exam = examOpt.get();
        // Eagerly load questions
        exam.getQuestions().size();
        List<Question> allQuestions = new java.util.ArrayList<>(exam.getQuestions());

        // If maxQuestions is null or 0, return all questions
        if (exam.getMaxQuestions() == null || exam.getMaxQuestions() <= 0) {
            return allQuestions;
        }

        // If we have fewer questions than maxQuestions, return all
        if (allQuestions.size() <= exam.getMaxQuestions()) {
            return allQuestions;
        }

        // Shuffle and return only maxQuestions
        Collections.shuffle(allQuestions);
        return allQuestions.subList(0, exam.getMaxQuestions());
    }

    /**
     * Get questions that are available in the question bank (all questions can be reused)
     * These can be mapped to multiple exams
     */
    public List<Question> getQuestionBankQuestions() {
        // All questions can be in the question bank and mapped to multiple exams
        List<Question> questions = questionRepository.findAll();
        questions.forEach(question -> {
            if (question.getCategory() != null) {
                question.getCategory().getId(); // Trigger lazy loading
                question.getCategory().getIsCommon(); // Ensure isCommon is loaded
            }
        });
        return questions;
    }

    /**
     * Get all questions that can be mapped to exams (question bank + all questions)
     */
    public List<Question> getAllMappableQuestions() {
        List<Question> questions = questionRepository.findAll();
        questions.forEach(question -> {
            if (question.getCategory() != null) {
                question.getCategory().getId(); // Trigger lazy loading
                question.getCategory().getIsCommon(); // Ensure isCommon is loaded
            }
        });
        return questions;
    }

    /**
     * Bulk create questions from Excel file
     */
    @Transactional
    public BulkUploadResponse bulkCreateQuestions(List<ExcelService.QuestionExcelRow> excelRows) {
        BulkUploadResponse response = new BulkUploadResponse();
        response.setTotalRows(excelRows.size());

        for (ExcelService.QuestionExcelRow excelRow : excelRows) {
            // Skip rows with errors
            if (excelRow.getError() != null && !excelRow.getError().isEmpty()) {
                response.getErrors().add(new BulkUploadResponse.ErrorDetail(
                    excelRow.getRowNumber(),
                    excelRow.getQuestionText() != null ? excelRow.getQuestionText() : "",
                    excelRow.getError()
                ));
                response.setFailureCount(response.getFailureCount() + 1);
                continue;
            }

            try {
                // Validate required fields
                if (excelRow.getQuestionText() == null || excelRow.getQuestionText().trim().isEmpty()) {
                    response.getErrors().add(new BulkUploadResponse.ErrorDetail(
                        excelRow.getRowNumber(),
                        "",
                        "Question text is required"
                    ));
                    response.setFailureCount(response.getFailureCount() + 1);
                    continue;
                }

                if (excelRow.getQuestionType() == null || excelRow.getQuestionType().trim().isEmpty()) {
                    response.getErrors().add(new BulkUploadResponse.ErrorDetail(
                        excelRow.getRowNumber(),
                        excelRow.getQuestionText(),
                        "Question type is required"
                    ));
                    response.setFailureCount(response.getFailureCount() + 1);
                    continue;
                }

                if (excelRow.getCorrectAnswer() == null || excelRow.getCorrectAnswer().trim().isEmpty()) {
                    response.getErrors().add(new BulkUploadResponse.ErrorDetail(
                        excelRow.getRowNumber(),
                        excelRow.getQuestionText(),
                        "Correct answer is required"
                    ));
                    response.setFailureCount(response.getFailureCount() + 1);
                    continue;
                }

                if (excelRow.getMarks() == null || excelRow.getMarks() <= 0) {
                    response.getErrors().add(new BulkUploadResponse.ErrorDetail(
                        excelRow.getRowNumber(),
                        excelRow.getQuestionText(),
                        "Marks must be greater than 0"
                    ));
                    response.setFailureCount(response.getFailureCount() + 1);
                    continue;
                }

                // Convert Excel row to Question entity
                Question question = excelService.convertToQuestion(excelRow, null);
                
                // Save question
                Question savedQuestion = questionRepository.save(question);
                response.getCreatedQuestions().add(savedQuestion);
                response.setSuccessCount(response.getSuccessCount() + 1);

            } catch (Exception e) {
                response.getErrors().add(new BulkUploadResponse.ErrorDetail(
                    excelRow.getRowNumber(),
                    excelRow.getQuestionText() != null ? excelRow.getQuestionText() : "",
                    e.getMessage() != null ? e.getMessage() : "Unknown error: " + e.getClass().getSimpleName()
                ));
                response.setFailureCount(response.getFailureCount() + 1);
            }
        }

        return response;
    }
}

