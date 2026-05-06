package com.example.hits.application.util;

import com.example.hits.infrastructure.persistence.entity.CourseEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.infrastructure.persistence.entity.UserCourseEntity;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class CourseUtility {
    public boolean isCourseAvailableForEditing(com.example.hits.infrastructure.persistence.entity.CourseEntity courseEntity, UserEntity userEntity) {
        var userCourse = getUserCourse(courseEntity, userEntity);
        return userCourse.isPresent() && userCourse.get().getUserRole() == UserCourseRole.HEAD_TEACHER;
    }

    public boolean isCourseAvailableForArchiving(CourseEntity courseEntity, UserEntity userEntity) {
        return isCourseAvailableForEditing(courseEntity, userEntity);
    }

    public boolean isUserAbleToLeaveCourse(CourseEntity courseEntity, UserEntity userEntity) {
        var userCourse = getUserCourse(courseEntity, userEntity);
        return userCourse.isPresent() && UserCourseRole.isUserLowerThan(userCourse.get().getUserRole(), UserCourseRole.HEAD_TEACHER);
    }

    public boolean isUserAvailableToChangeOtherUserRoleOnCourse(
            CourseEntity courseEntity,
            UserEntity userEntity,
            UserCourseRole newUserCourseRole,
            UserEntity requestingUserEntity
    ) {
        var requestingUserCourse = getUserCourse(courseEntity, requestingUserEntity);
        var userCourse = getUserCourse(courseEntity, userEntity);

        if (userCourse.isEmpty() || requestingUserCourse.isEmpty()) {
            return false;
        }

        return UserCourseRole.isUserHigherThan(requestingUserCourse.get().getUserRole(), newUserCourseRole)
                && UserCourseRole.isUserHigherThan(requestingUserCourse.get().getUserRole(), userCourse.get().getUserRole());
    }

    public boolean isUserAvailableToRemoveOtherUserFromCourse(
            CourseEntity courseEntity,
            UserEntity userEntity,
            UserEntity requestingUserEntity
    ) {
        var requestingUserCourse = getUserCourse(courseEntity, requestingUserEntity);
        var userCourse = getUserCourse(courseEntity, userEntity);

        if (userCourse.isEmpty() || requestingUserCourse.isEmpty()) {
            return false;
        }

        return UserCourseRole.isUserHigherThan(requestingUserCourse.get().getUserRole(), userCourse.get().getUserRole());
    }

    public Optional<UserCourseEntity> getUserCourse(com.example.hits.infrastructure.persistence.entity.CourseEntity courseEntity, com.example.hits.infrastructure.persistence.entity.UserEntity userEntity) {
        return courseEntity.getCourseUsers().stream()
                .filter(uc -> uc.getUserEntity().equals(userEntity))
                .findFirst();
    }
}
