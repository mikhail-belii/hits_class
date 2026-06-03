package com.example.hits.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Entity
@Table(name = "criteria_score")
@Getter
@Setter
@Accessors(chain = true)
public class CriteriaScoreEntity {

    @Id
    private UUID id;

    private Float score;

    @ManyToOne
    @JoinColumn(name = "mark_criteria_id")
    private MarkCriteriaEntity markCriteriaEntity;

    // Оценка по критерию либо для оценки студента
    @ManyToOne
    @JoinColumn(name = "task_answer_student_appraiser_id")
    private TaskAnswerStudentAppraiserEntity taskAnswerStudentAppraiserEntity;

    // Либо для оценки преподавателя
    @ManyToOne
    @JoinColumn(name = "task_answer_id")
    private TaskAnswerEntity taskAnswerEntity;

}
