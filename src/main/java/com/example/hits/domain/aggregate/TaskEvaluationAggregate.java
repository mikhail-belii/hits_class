package com.example.hits.domain.aggregate;

import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class TaskEvaluationAggregate {

    private final TaskAnswer taskAnswer;

    private final UserCourse userCourse;

    private final Post post;

    public void evaluateTask(int score) {
        if (userCourse.getUserRole() != UserCourseRole.TEACHER) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        if (post.getMaxScore() < score|| score < 0) {
            throw ExceptionUtility.badRequestException("Invalid score");
        }

        taskAnswer.setScore(score);
    }

}
