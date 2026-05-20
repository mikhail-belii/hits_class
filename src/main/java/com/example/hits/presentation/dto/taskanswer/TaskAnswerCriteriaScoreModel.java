package com.example.hits.presentation.dto.taskanswer;

import com.example.hits.domain.entity.markCriteria.EvaluationFunction;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class TaskAnswerCriteriaScoreModel {

    private UUID markCriteriaId;

    private String name;

    private String description;

    private Float score;

    private Float minScore;

    private Float maxScore;

    private Float multiplier;

    private EvaluationFunction evaluationFunction;
}
