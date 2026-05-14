package com.example.hits.domain.aggregate;

import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.course.CourseMarkEvaluationType;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import com.example.hits.domain.utility.MathUtility;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.example.hits.domain.entity.post.PostType.TASK;

@RequiredArgsConstructor
@Getter
public class CourseEvaluationAggregate {

    private final static Float courseMaxScore = 5.3F;

    private final static Float courseMinScore = 1.7F;

    private final UserCourse userCourse;

    private final Course course;

    private final List<ScoredPost> scoredPosts;

    public void evaluateCourseByTasks() {
        validatePosts();
        validateUser();

        switch (course.getCourseMarkEvaluationType()) {
            case SUM -> sumScoreEvaluation();
            case MEAN_VALUE -> meanValueScoreEvaluation();
            case COEFFICIENTS_SUM -> coefficientsSumScoreEvaluation();
            case COEFFICIENTS_MEAN_VALUE -> coefficientsMeanValueScoreEvaluation();
            case PASS_FAIL -> passFailScoreEvaluation();
            default -> throw ExceptionUtility.badRequestException("Invalid courseMarkEvaluationType", "courseMarkEvaluationType");
        }
    }

    private void sumScoreEvaluation() {
        Float currentScore = 0F;
        for (ScoredPost scoredPost : scoredPosts) {
            currentScore += scoredPost.getScoreByMarkEvaluationType(CourseMarkEvaluationType.SUM);
        }
        Float finalScore = MathUtility.getValueByDiapasons(courseMinScore, currentScore, courseMaxScore);
        userCourse.setScore(finalScore);
    }

    private void meanValueScoreEvaluation() {
        Float currentScore = 0F;
        for (ScoredPost scoredPost : scoredPosts) {
            currentScore += MathUtility.interpolateValueByDiapasons(
                    scoredPost.getMinScore(),
                    scoredPost.getScoreByMarkEvaluationType(CourseMarkEvaluationType.MEAN_VALUE),
                    scoredPost.getMaxScore(),
                    courseMinScore,
                    courseMaxScore);
        }
        Float finalScore = MathUtility.getValueByDiapasons(courseMinScore, currentScore / scoredPosts.size(), courseMaxScore);
        userCourse.setScore(finalScore);
    }

    private void coefficientsSumScoreEvaluation() {
        Float currentScore = 0F;
        for (ScoredPost scoredPost : scoredPosts) {
            currentScore = scoredPost
                    .getEvaluationFunction()
                    .performEvaluation(
                            scoredPost.getScoreByMarkEvaluationType(CourseMarkEvaluationType.COEFFICIENTS_SUM),
                            currentScore);
        }
        Float finalScore = MathUtility.getValueByDiapasons(courseMinScore, currentScore, courseMaxScore);
        userCourse.setScore(finalScore);
    }

    private void coefficientsMeanValueScoreEvaluation() {
        Float currentScore = 0F;
        for (ScoredPost scoredPost : scoredPosts) {
            currentScore += scoredPost.getScoreByMarkEvaluationType(CourseMarkEvaluationType.COEFFICIENTS_MEAN_VALUE);
        }
        Float finalScore = MathUtility.getValueByDiapasons(courseMinScore, currentScore / scoredPosts.size(), courseMaxScore);
        userCourse.setScore(finalScore);
    }

    private void passFailScoreEvaluation() {
        Float currentScore = 0F;
        for (ScoredPost scoredPost : scoredPosts) {
            currentScore += MathUtility.interpolateValueByDiapasons(
                    scoredPost.getMinScore(),
                    scoredPost.getScoreByMarkEvaluationType(CourseMarkEvaluationType.PASS_FAIL),
                    scoredPost.getMaxScore(),
                    0F,
                    1F);
        }
        Float finalScore = currentScore / scoredPosts.size();
        if (course.getPassThreshold() < finalScore) {
            userCourse.setScore(1F);
        } else {
            userCourse.setScore(0F);
        }
    }

    private void validateUser() {
        if (userCourse.getUserRole() != UserCourseRole.STUDENT) {
            throw ExceptionUtility.badRequestException("Cannot evaluate task answer due to course member is not a student");
        }
    }

    private void validatePosts() {
        if (scoredPosts.stream().anyMatch(scoredPost -> !TASK.equals(scoredPost.getPostType()))) {
            throw ExceptionUtility.badRequestException("Cannot evaluate course score due to wrong post type of one of the posts", "postType");
        }
    }

}
