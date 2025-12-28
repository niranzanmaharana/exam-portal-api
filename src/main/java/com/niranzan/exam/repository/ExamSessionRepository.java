package com.niranzan.exam.repository;

import com.niranzan.exam.entity.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {
    List<ExamSession> findByStudentId(Long studentId);
    List<ExamSession> findByExamId(Long examId);
    Optional<ExamSession> findByStudentIdAndExamIdAndStatus(Long studentId, Long examId, ExamSession.SessionStatus status);
}

