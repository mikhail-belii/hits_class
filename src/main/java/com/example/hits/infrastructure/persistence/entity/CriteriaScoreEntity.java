package com.example.hits.infrastructure.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Entity
@Table(name = "criteria_score")
@Data
@Accessors(chain = true)
public class CriteriaScoreEntity {

    @Id
    private UUID id;

    private Float score;

    @ManyToOne
    @JoinColumn(name = "mark_criteria_id")
    private MarkCriteriaEntity markCriteriaEntity;

    @ManyToOne
    @JoinColumn(name = "task_answer_id")
    private TaskAnswerEntity taskAnswerEntity;

}
