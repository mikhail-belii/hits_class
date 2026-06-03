package com.example.hits.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
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

    @OneToMany(mappedBy = "taskAnswerStudentAppraiserEntity")
    private List<CriteriaScoreEntity> criteriaScores;

    private LocalDateTime submittedAt;

}