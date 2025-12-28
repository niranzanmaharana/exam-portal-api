package com.niranzan.exam.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ExamSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_id", nullable = false)
    private Long examId;

    @Column(name = "student_id")
    private Long studentId; // Nullable for public exam access

    @Column(name = "student_name")
    private String studentName; // For public exam access

    @Column(name = "registration_number")
    private String registrationNumber; // For public exam access

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "exception_reason", columnDefinition = "TEXT")
    private String exceptionReason; // Reason for abrupt closure (tab switch, focus loss, etc.)

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum SessionStatus {
        IN_PROGRESS, SUBMITTED, TIMED_OUT, TERMINATED
    }
}

