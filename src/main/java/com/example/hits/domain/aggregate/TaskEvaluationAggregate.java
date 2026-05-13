package com.example.hits.domain.aggregate;

import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.domain.entity.post.TaskMarkEvaluationType;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import com.example.hits.domain.utility.MathUtility;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class TaskEvaluationAggregate {

    private final static List<Float> PASS_FAIL_SCORES = List.of(0f, 1f);

    private final TaskAnswer taskAnswer;

    private final UserCourse userCourse;

    private final Post post;

    private final List<ScoredMarkCriteria> scoredMarkCriteriaList;

    public void evaluateTaskManually(Float score, UserCourse requestingUserCourse) {
        validatePost();
        validateUsers(requestingUserCourse);

        if (!post.getTaskMarkEvaluationType().isAnswerScoreMustBeSetManually()) {
            throw  ExceptionUtility.badRequestException("Cannot evaluate task answer score manually due to not compatible markEvaluationType", "postType");
        }

        if (post.getMaxScore() < score || score < post.getMinScore()) {
            throw ExceptionUtility.badRequestException("Invalid score");
        }

        if (post.getTaskMarkEvaluationType().isAnswerScoreIsPassFail() && !PASS_FAIL_SCORES.contains(score)) {
            throw ExceptionUtility.badRequestException("Invalid score");
        }

        taskAnswer.setScore(score);
    }

    public void evaluateTaskByCriteriaList() {
        validatePost();

        if (!post.getTaskMarkEvaluationType().isAnswerScoreIsCriteriaBased()) {
            throw  ExceptionUtility.badRequestException("Cannot evaluate task answer score by criterias due to not compatible markEvaluationType", "postType");
        }

        switch (post.getTaskMarkEvaluationType()) {
            case SUM -> sumScoreEvaluation();
            case MEAN_VALUE -> meanValueScoreEvaluation();
            case COEFFICIENTS_SUM -> coefficientsSumScoreEvaluation();
            case COEFFICIENTS_MEAN_VALUE -> coefficientsMeanValueScoreEvaluation();
            case SELF_ASSESSMENT -> selfAssessmentScoreEvaluation();
            case PASS_FAIL -> passFailScoreEvaluation();
            default -> throw ExceptionUtility.badRequestException("Invalid taskMarkEvaluationType", "taskMarkEvaluationType");
        }
    }

    private void sumScoreEvaluation() {
        Float currentScore = 0F;
        for (ScoredMarkCriteria scoredMarkCriteria : scoredMarkCriteriaList) {
            currentScore += scoredMarkCriteria.getScoreByMarkEvaluationType(TaskMarkEvaluationType.SUM);
        }
        Float finalScore = MathUtility.getValueByDiapasons(post.getMinScore(), currentScore, post.getMaxScore());
        taskAnswer.setScore(finalScore);
    }

    private void meanValueScoreEvaluation() {
        Float currentScore = 0F;
        for (ScoredMarkCriteria scoredMarkCriteria : scoredMarkCriteriaList) {
            currentScore += scoredMarkCriteria.getScoreByMarkEvaluationType(TaskMarkEvaluationType.MEAN_VALUE);
        }
        Float finalScore = MathUtility.getValueByDiapasons(post.getMinScore(), currentScore / scoredMarkCriteriaList.size(), post.getMaxScore());
        taskAnswer.setScore(finalScore);
    }

    private void coefficientsSumScoreEvaluation() {
        Float currentScore = 0F;
        for (ScoredMarkCriteria scoredMarkCriteria : scoredMarkCriteriaList) {
            currentScore = scoredMarkCriteria
                    .getEvaluationFunction()
                    .performEvaluation(
                            scoredMarkCriteria.getScoreByMarkEvaluationType(TaskMarkEvaluationType.COEFFICIENTS_SUM),
                            currentScore);
        }
        Float finalScore = MathUtility.getValueByDiapasons(post.getMinScore(), currentScore, post.getMaxScore());
        taskAnswer.setScore(finalScore);
    }

    private void coefficientsMeanValueScoreEvaluation() {
        Float currentScore = 0F;
        for (ScoredMarkCriteria scoredMarkCriteria : scoredMarkCriteriaList) {
            currentScore = scoredMarkCriteria.getScoreByMarkEvaluationType(TaskMarkEvaluationType.COEFFICIENTS_MEAN_VALUE);
        }
        Float finalScore = MathUtility.getValueByDiapasons(post.getMinScore(), currentScore / scoredMarkCriteriaList.size(), post.getMaxScore());
        taskAnswer.setScore(finalScore);
    }

    private void selfAssessmentScoreEvaluation() {
        Float currentScore = 0F;
        for (ScoredMarkCriteria scoredMarkCriteria : scoredMarkCriteriaList) {
            currentScore = scoredMarkCriteria.getScoreByMarkEvaluationType(TaskMarkEvaluationType.SELF_ASSESSMENT);
        }
        Float finalScore = MathUtility.getValueByDiapasons(post.getMinScore(), currentScore, post.getMaxScore());
        taskAnswer.setScore(finalScore);
    }

    private void passFailScoreEvaluation() {
        Float currentScore = 0F;
        for (ScoredMarkCriteria scoredMarkCriteria : scoredMarkCriteriaList) {
            currentScore = scoredMarkCriteria.getScoreByMarkEvaluationType(TaskMarkEvaluationType.PASS_FAIL);
        }
        Float finalScore = currentScore / scoredMarkCriteriaList.size();
        taskAnswer.setScore(finalScore);
    }

    private void validateUsers(UserCourse requestingUserCourse) {
        if (requestingUserCourse.getUserRole() == UserCourseRole.STUDENT) {
            throw ExceptionUtility.forbiddenRightsException();
        }
        if (userCourse.getUserRole() != UserCourseRole.STUDENT) {
            throw ExceptionUtility.badRequestException("Cannot evaluate task answer due to author is not a student");
        }
    }

    private void validatePost() {
        if (!PostType.TASK.equals(post.getPostType())) {
            throw ExceptionUtility.badRequestException("Cannot evaluate task answer score due to wrong post type", "postType");
        }
        if (post.getTaskMarkEvaluationType() == null) {
            throw ExceptionUtility.internalServerError("post with postType = TASK has markEvaluationType = null");
        }
    }

}
