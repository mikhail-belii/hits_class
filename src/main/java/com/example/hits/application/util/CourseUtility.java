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
        return false;
    }

    public boolean isCourseAvailableForArchiving(Course course, User user) {
        return false;
    }

    public boolean isUserAvailableToChangeOtherUserRoleOnCourse(
            Course course,
            User user,
            UserCourseRole newUserCourseRole,
            User requestingUser
    ) {
        return false;
    }

    public boolean isUserAvailableToRemoveOtherUserFromCourse(
            Course course,
            User user,
            UserCourseRole newUserCourseRole,
            User requestingUser
    ) {
        return false;
    }

    private Optional<UserCourse> getUserCourse(Course course, User user) {
        return course.getCourseUsers().stream()
                .filter(uc -> uc.getUser().equals(user))
                .findFirst();
    }
}
