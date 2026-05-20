package com.example.hits.presentation.request.markcriteria;

import com.example.hits.domain.entity.markCriteria.EvaluationFunction;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@Accessors(chain = true)
public class MarkCriteriaWriteRequest {

    private String name;

    @Length(max = 2048)
    private String description;

    private Float minScore;

    private Float maxScore;

    private Float multiplier;

    private EvaluationFunction evaluationFunction;
}
