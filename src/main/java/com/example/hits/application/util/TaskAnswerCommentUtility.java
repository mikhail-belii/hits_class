package com.example.hits.application.util;

import com.example.hits.infrastructure.persistence.entity.CourseEntity;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerCommentEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerEntity;
import com.example.hits.infrastructure.persistence.entity.UserCourseEntity;
import lombok.experimental.UtilityClass;

import java.util.Objects;
import java.util.Optional;

@UtilityClass
public class TaskAnswerCommentUtility {

    public boolean isCommentAvailableForEditing(TaskAnswerCommentEntity taskAnswerCommentEntity, UserEntity userEntity) {
        return Objects.equals(userEntity, taskAnswerCommentEntity.getAuthor());
    }

    public boolean isTaskAnswerCommentsAvailableForUser(TaskAnswerEntity taskAnswer, com.example.hits.infrastructure.persistence.entity.UserEntity userEntity) {
        CourseEntity course = taskAnswer.getPostEntity().getCourseEntity();
        Optional<UserCourseEntity> userCourse = CourseUtility.getUserCourse(course, userEntity);

        if (userCourse.isEmpty()) {
            return false;
        }

        return Objects.equals(taskAnswer.getUserEntity(), userCourse.get().getUserEntity())
                || UserCourseRole.isUserHigherThan(userCourse.get().getUserRole(), UserCourseRole.STUDENT);
    }

}
