package com.example.hits.application.util;

import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class CourseUtility {
    public boolean isCourseAvailableForEditing(Course course, User user) {
        var userCourse = getUserCourse(course, user);
        return userCourse.isPresent() && userCourse.get().getUserRole() == UserCourseRole.HEAD_TEACHER;
    }

    public boolean isCourseAvailableForArchiving(Course course, User user) {
        return isCourseAvailableForEditing(course, user);
    }

    public boolean isUserAbleToLeaveCourse(Course course, User user) {
    }

    public boolean isUserAvailableToChangeOtherUserRoleOnCourse(
            Course course,
            User user,
            UserCourseRole newUserCourseRole,
            User requestingUser
    ) {
        var requestingUserCourse = getUserCourse(course, requestingUser);
        var userCourse = getUserCourse(course, user);

        if (userCourse.isEmpty() || requestingUserCourse.isEmpty()) {
            return false;
        }

        return UserCourseRole.isUserHigherThan(requestingUserCourse.get().getUserRole(), newUserCourseRole)
                && UserCourseRole.isUserHigherThan(requestingUserCourse.get().getUserRole(), userCourse.get().getUserRole());
    }

    public boolean isUserAvailableToRemoveOtherUserFromCourse(
            Course course,
            User user,
            User requestingUser
    ) {
        var requestingUserCourse = getUserCourse(course, requestingUser);
        var userCourse = getUserCourse(course, user);

        if (userCourse.isEmpty() || requestingUserCourse.isEmpty()) {
            return false;
        }

        return UserCourseRole.isUserHigherThan(requestingUserCourse.get().getUserRole(), userCourse.get().getUserRole());
    }

    public Optional<UserCourse> getUserCourse(Course course, User user) {
        return course.getCourseUsers().stream()
                .filter(uc -> uc.getUser().equals(user))
                .findFirst();
    }
}
