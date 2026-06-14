package com.example.hits.domain.aggregate;

import com.example.hits.domain.entity.course.CourseMarkEvaluationType;
import com.example.hits.domain.entity.markCriteria.EvaluationFunction;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.domain.entity.post.TaskMarkEvaluationType;
import com.example.hits.domain.utility.MathUtility;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public class ScoredPost {

    private final PostType postType;

    private final TaskMarkEvaluationType taskMarkEvaluationType;

    private final Float multiplier;

    private final EvaluationFunction evaluationFunction;

    private final Float score;

    private final Float teacherScore;

    private final Float minScore;

    private final Float maxScore;

    public Float getScoreByMarkEvaluationType(CourseMarkEvaluationType markEvaluationType) {
        Float score = this.score;
        if (teacherScore != null) {
            score = teacherScore;
        }
        if (score == null) {
            return 0F;
        }
        return switch (markEvaluationType) {
            case SUM -> MathUtility.getValueByDiapasons(minScore, score, maxScore);
            case MEAN_VALUE -> MathUtility.getValueByDiapasons(minScore, score, maxScore);
            case COEFFICIENTS_SUM -> MathUtility.getValueByDiapasons(minScore, score, maxScore) * multiplier;
            case COEFFICIENTS_MEAN_VALUE -> MathUtility.getValueByDiapasons(minScore, score, maxScore) * multiplier;
            case PASS_FAIL -> score;
            default -> minScore;
        };
    }

}
