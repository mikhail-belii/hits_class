package com.example.hits.domain.entity.markCriteria;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class MarkCriteria {

    private UUID id;

    private EvaluationFunction evaluationFunction;

    private String name;

    private Float multiplier;

    private Float minScore;

    private Float maxScore;

    private Float passThreshold;

    private UUID postId;

}
