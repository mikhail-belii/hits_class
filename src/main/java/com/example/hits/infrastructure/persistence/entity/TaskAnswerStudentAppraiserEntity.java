package com.example.hits.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_answer_student_appraiser")
@Data
@Accessors(chain = true)
public class TaskAnswerStudentAppraiserEntity {

    @Id
    private UUID id;

    private Float score;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private UserEntity student;

    @ManyToOne
    @JoinColumn(name = "task_answer_id")
    private TaskAnswerEntity taskAnswerEntity;

    @ManyToOne
    @JoinColumn(name = "criteria_score_id")
    private TaskAnswerEntity criteriaScore;

    private LocalDateTime submittedAt;

}