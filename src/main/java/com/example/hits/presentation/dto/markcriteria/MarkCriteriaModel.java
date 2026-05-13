package com.example.hits.presentation.dto.markcriteria;

import com.example.hits.domain.entity.markCriteria.EvaluationFunction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MarkCriteriaModel {

    private UUID id;

    private EvaluationFunction evaluationFunction;

    private String name;

    private Float multiplier;

    private Float minScore;

    private Float maxScore;

    private UUID postId;
}
