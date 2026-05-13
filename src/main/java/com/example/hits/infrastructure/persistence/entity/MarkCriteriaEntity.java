package com.example.hits.infrastructure.persistence.entity;

import com.example.hits.domain.entity.markCriteria.EvaluationFunction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Entity
@Table(name = "mark_criteria")
@Data
@Accessors(chain = true)
public class MarkCriteriaEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private EvaluationFunction evaluationFunction;

    @NotNull
    @Length(max = 2048)
    private String name;

    private Float multiplier;

    private Float minScore;

    private Float maxScore;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private PostEntity postEntity;

}
