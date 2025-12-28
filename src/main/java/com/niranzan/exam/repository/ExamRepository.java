package com.niranzan.exam.repository;

import com.niranzan.exam.entity.Exam;
import com.niranzan.exam.entity.Question;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByCreatedBy(Long createdBy);

    List<Exam> findByStatus(Exam.ExamStatus status);

    Optional<Exam> findByAccessCode(String accessCode);

    boolean existsByAccessCode(String accessCode);

    /**
     * Get all questions mapped to an exam via the many-to-many relationship
     * Eagerly fetches category to avoid lazy loading issues
     */
    @Query("SELECT DISTINCT q FROM Question q " +
            "LEFT JOIN FETCH q.category " +
            "JOIN q.exams e WHERE e.id = :examId")
    List<Question> findQuestionsByExamId(@Param("examId") Long examId);

    /**
     * Load Exam with Questions (LAZY by default, eager only here)
     */
    @EntityGraph(attributePaths = {"questions"})
    @Query("SELECT e FROM Exam e WHERE e.id = :examId")
    Optional<Exam> findByIdWithQuestions(@Param("examId") Long examId);

    /**
     * Load Exam with Questions + Question Category
     */
    @EntityGraph(attributePaths = {"questions", "questions.category"})
    @Query("SELECT e FROM Exam e WHERE e.id = :examId")
    Optional<Exam> findByIdWithQuestionsAndCategory(@Param("examId") Long examId);
}

