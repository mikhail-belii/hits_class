package com.example.hits.domain.entity.markCriteria;

public record MarkCriteriaDefinition(String name, String description, Float minScore, Float maxScore, Float multiplier, EvaluationFunction evaluationFunction) {
}
