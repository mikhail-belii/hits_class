package com.example.hits.application.util;

import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class PostUtility {
    public boolean isPostAvailableForReading(Course course, Post post, User user) {
        return isUserInCourse(course, user) && post.getCourse().equals(course);
    }

    public boolean isAvailableForEditing(Course course, User user) {
        return getUserCourse(course, user)
                .map(PostUtility::isUserTeacher)
                .orElse(false);
    }

    public boolean isUserInCourse(Course course, User user) {
        return course.getCourseUsers().stream()
                .anyMatch(uc -> uc.getUser().equals(user));
    }

    private Optional<UserCourse> getUserCourse(Course course, User user) {
        return course.getCourseUsers().stream()
                .filter(uc -> uc.getUser().equals(user))
                .findFirst();
    }

    private boolean isUserTeacher(UserCourse userCourse) {
        return userCourse.getUserRole() == UserCourseRole.TEACHER ||
                userCourse.getUserRole() == UserCourseRole.HEAD_TEACHER;
    }
}
