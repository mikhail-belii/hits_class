package com.example.hits.application.util;

import com.example.hits.infrastructure.persistence.entity.CourseEntity;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.infrastructure.persistence.entity.PostEntity;
import com.example.hits.infrastructure.persistence.entity.UserCourseEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class PostUtility {
    public boolean isPostAvailableForReading(CourseEntity course, PostEntity post, UserEntity user) {
        return isUserInCourse(course, user) && post.getCourseEntity().equals(course);
    }

    public boolean isAvailableForEditing(CourseEntity course, UserEntity user) {
        return getUserCourse(course, user)
                .map(PostUtility::isUserTeacher)
                .orElse(false);
    }

    public boolean isUserInCourse(CourseEntity course, UserEntity user) {
        return course.getCourseUsers().stream()
                .anyMatch(uc -> uc.getUserEntity().equals(user));
    }

    public Optional<UserCourseEntity> getUserCourse(CourseEntity course, UserEntity user) {
        return course.getCourseUsers().stream()
                .filter(uc -> uc.getUserEntity().equals(user))
                .findFirst();
    }

    private boolean isUserTeacher(UserCourseEntity userCourse) {
        return userCourse.getUserRole() == UserCourseRole.TEACHER ||
                userCourse.getUserRole() == UserCourseRole.HEAD_TEACHER;
    }
}
