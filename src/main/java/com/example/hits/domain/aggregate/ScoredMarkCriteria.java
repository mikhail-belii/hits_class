package com.example.hits.domain.aggregate;

import com.example.hits.domain.entity.markCriteria.EvaluationFunction;
import com.example.hits.domain.entity.post.TaskMarkEvaluationType;
import com.example.hits.domain.utility.MathUtility;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ScoredMarkCriteria {

    private final EvaluationFunction evaluationFunction;

    private final String name;

    private final Float multiplier;

    private final Float minScore;

    private final Float score;

    private final Float maxScore;

    private final UUID postId;

    public Float getScoreByMarkEvaluationType(TaskMarkEvaluationType markEvaluationType) {
        return switch (markEvaluationType) {
            case SUM -> MathUtility.getValueByDiapasons(minScore, score, maxScore);
            case MEAN_VALUE -> MathUtility.getValueByDiapasons(minScore, score, maxScore);
            case COEFFICIENTS_SUM -> MathUtility.getValueByDiapasons(minScore, score, maxScore) * multiplier;
            case COEFFICIENTS_MEAN_VALUE -> MathUtility.getValueByDiapasons(minScore, score, maxScore) * multiplier;
            case PASS_FAIL -> score;
            case SELF_ASSESSMENT -> MathUtility.getValueByDiapasons(minScore, score, maxScore);
            default -> minScore;
        };
    }

    public EvaluationFunction getEvaluationFunction() {
        return evaluationFunction;
    }

}
