package com.example.hits.domain.entity.markCriteria;

import com.example.hits.domain.entity.post.TaskMarkEvaluationType;
import com.example.hits.domain.exception.DomainRuleViolationException;
import lombok.Getter;

import java.util.UUID;

@Getter
public class MarkCriteria {

    private UUID id;
    private UUID postId;
    private String name;
    private Float multiplier;
    private Float minScore;
    private Float maxScore;
    private EvaluationFunction evaluationFunction;

    private MarkCriteria() {
    }

    public static MarkCriteria issue(UUID id,
                                     UUID postId,
                                     MarkCriteriaDefinition definition,
                                     TaskMarkEvaluationType taskEvaluationType) {
        validateAgainstTaskType(definition, taskEvaluationType);
        MarkCriteria m = new MarkCriteria();
        m.id = id;
        m.postId = postId;
        applyDefinition(m, definition);
        m.evaluationFunction = EvaluationFunction.SUM;
        return m;
    }

    public static MarkCriteria restore(UUID id,
                                       UUID postId,
                                       String name,
                                       Float minScore,
                                       Float maxScore,
                                       Float multiplier,
                                       EvaluationFunction evaluationFunction) {
        MarkCriteria m = new MarkCriteria();
        m.id = id;
        m.postId = postId;
        m.name = name;
        m.minScore = minScore;
        m.maxScore = maxScore;
        m.multiplier = multiplier;
        m.evaluationFunction = evaluationFunction;
        return m;
    }

    public void redefine(MarkCriteriaDefinition definition, TaskMarkEvaluationType taskEvaluationType) {
        validateAgainstTaskType(definition, taskEvaluationType);
        applyDefinition(this, definition);
        this.evaluationFunction = EvaluationFunction.SUM;
    }

    private static void applyDefinition(MarkCriteria target, MarkCriteriaDefinition definition) {
        String rawName = definition.name();
        target.name = rawName == null ? null : rawName.trim();
        target.minScore = definition.minScore();
        target.maxScore = definition.maxScore();
        target.multiplier = definition.multiplier();
    }

    private static void validateAgainstTaskType(MarkCriteriaDefinition definition,
                                                TaskMarkEvaluationType evaluationType) {
        if (evaluationType == null) {
            throw new DomainRuleViolationException("Task mark evaluation type is not set on the post");
        }

        String name = definition.name();
        Float minScore = definition.minScore();
        Float maxScore = definition.maxScore();
        Float multiplier = definition.multiplier();

        switch (evaluationType) {
            case TEACHER_DECISION, TEACHER_DECISION_PASS_FAIL -> throw new DomainRuleViolationException(
                    "Mark criteria cannot be used for this task evaluation type");
            case SUM, MEAN_VALUE, SELF_ASSESSMENT -> {
                requireNonBlankName(name);
                requireScores(minScore, maxScore);
                if (multiplier != null) {
                    throw new DomainRuleViolationException("multiplier must be null for this task evaluation type");
                }
            }
            case COEFFICIENTS_SUM, COEFFICIENTS_MEAN_VALUE -> {
                requireNonBlankName(name);
                requireScores(minScore, maxScore);
                if (multiplier == null) {
                    throw new DomainRuleViolationException("multiplier is required for this task evaluation type");
                }
            }
            case PASS_FAIL -> {
                requireNonBlankName(name);
                if (minScore != null || maxScore != null || multiplier != null) {
                    throw new DomainRuleViolationException(
                            "Only name is allowed for mark criteria for this task evaluation type");
                }
            }
        }
    }

    private static void requireNonBlankName(String name) {
        if (name == null || name.isBlank()) {
            throw new DomainRuleViolationException("Mark criteria name must be not blank");
        }
    }

    private static void requireScores(Float minScore, Float maxScore) {
        if (minScore == null || maxScore == null) {
            throw new DomainRuleViolationException(
                    "minScore and maxScore are required for this task evaluation type");
        }
        if (minScore < 0 || maxScore < 0) {
            throw new DomainRuleViolationException("minScore and maxScore must be non-negative");
        }
        if (minScore > maxScore) {
            throw new DomainRuleViolationException("minScore must not be greater than maxScore");
        }
    }
}
