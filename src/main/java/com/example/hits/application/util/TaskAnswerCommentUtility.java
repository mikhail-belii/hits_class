package com.example.hits.application.util;

import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.taskanswercomment.TaskAnswerComment;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class TaskAnswerCommentUtility {

    public boolean isCommentAvailableForEditing(TaskAnswerComment taskAnswerComment, User user) {
        return false;
    }

    public boolean isTaskAnswerCommentsAvailableForUser(TaskAnswer taskAnswer, User user) {
        return false;
    }

}
