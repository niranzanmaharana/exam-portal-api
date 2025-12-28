package com.niranzan.exam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "exams")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer duration; // in minutes

    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks;

    @Column(name = "passing_marks", nullable = false)
    private Integer passingMarks;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExamStatus status;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "access_code", unique = true, length = 20)
    private String accessCode; // Unique code for public exam access

    @Column(name = "rules_and_restrictions", columnDefinition = "TEXT")
    private String rulesAndRestrictions; // Custom rules and restrictions for this exam

    @Column(name = "max_questions")
    private Integer maxQuestions; // Maximum number of questions to show (null = show all)

    // Exam restrictions
    @Column(name = "require_fullscreen", nullable = false)
    private Boolean requireFullscreen = false; // Require fullscreen mode during exam

    @Column(name = "disable_right_click", nullable = false)
    private Boolean disableRightClick = false; // Disable right-click during exam

    @Column(name = "require_camera", nullable = false)
    private Boolean requireCamera = false; // Require camera access during exam

    @Column(name = "disable_copy_paste", nullable = false)
    private Boolean disableCopyPaste = false; // Disable copy/paste during exam

    @Column(name = "disable_print_screen", nullable = false)
    private Boolean disablePrintScreen = false; // Disable print screen during exam

    @Column(name = "prevent_tab_switch", nullable = false)
    private Boolean preventTabSwitch = false; // Prevent switching browser tabs and window focus loss

    @ManyToMany
    @JoinTable(
            name = "exam_questions",
            joinColumns = @JoinColumn(name = "exam_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private Set<Question> questions = new HashSet<>();

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum ExamStatus {
        DRAFT, PUBLISHED, ACTIVE, COMPLETED
    }
}
