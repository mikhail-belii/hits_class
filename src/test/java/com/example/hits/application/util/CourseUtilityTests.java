package com.example.hits.application.util;

import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CourseUtilityTests {
    @Test
    void isCourseAvailableForEditing_whenUserIsHeadTeacher_returnsTrue() {
        User user = createUser();
        Course course = createCourse(List.of(createUserCourse(user, UserCourseRole.HEAD_TEACHER)));

        assertTrue(CourseUtility.isCourseAvailableForEditing(course, user));
    }

    @Test
    void isCourseAvailableForEditing_whenUserIsTeacher_returnsFalse() {
        User user = createUser();
        Course course = createCourse(List.of(createUserCourse(user, UserCourseRole.TEACHER)));

        assertFalse(CourseUtility.isCourseAvailableForEditing(course, user));
    }

    @Test
    void isCourseAvailableForEditing_whenUserIsStudent_returnsFalse() {
        User user = createUser();
        Course course = createCourse(List.of(createUserCourse(user, UserCourseRole.STUDENT)));

        assertFalse(CourseUtility.isCourseAvailableForEditing(course, user));
    }

    @Test
    void isCourseAvailableForEditing_whenUserNotInCourse_returnsFalse() {
        User user = createUser();
        User anotherUser = createUser();
        Course course = createCourse(List.of(createUserCourse(anotherUser, UserCourseRole.HEAD_TEACHER)));

        assertFalse(CourseUtility.isCourseAvailableForEditing(course, user));
    }

    @Test
    void isCourseAvailableForArchiving_whenUserIsHeadTeacher_returnsTrue() {
        User user = createUser();
        Course course = createCourse(List.of(createUserCourse(user, UserCourseRole.HEAD_TEACHER)));

        assertTrue(CourseUtility.isCourseAvailableForArchiving(course, user));
    }

    @Test
    void isCourseAvailableForArchiving_whenUserIsTeacher_returnsFalse() {
        User user = createUser();
        Course course = createCourse(List.of(createUserCourse(user, UserCourseRole.TEACHER)));

        assertFalse(CourseUtility.isCourseAvailableForArchiving(course, user));
    }

    @Test
    void isUserAvailableToChangeOtherUserRoleOnCourse_whenRequestingUserIsHigherThanBothNewRoleAndTargetUser_returnsTrue() {
        User requestingUser = createUser();
        User targetUser = createUser();
        Course course = createCourse(List.of(
                createUserCourse(requestingUser, UserCourseRole.HEAD_TEACHER),
                createUserCourse(targetUser, UserCourseRole.STUDENT)
        ));

        assertTrue(CourseUtility.isUserAvailableToChangeOtherUserRoleOnCourse(
                course, targetUser, UserCourseRole.TEACHER, requestingUser
        ));
    }

    @Test
    void isUserAvailableToChangeOtherUserRoleOnCourse_whenRequestingUserIsNotHigherThanNewRole_returnsFalse() {
        User requestingUser = createUser();
        User targetUser = createUser();
        Course course = createCourse(List.of(
                createUserCourse(requestingUser, UserCourseRole.TEACHER),
                createUserCourse(targetUser, UserCourseRole.STUDENT)
        ));

        assertFalse(CourseUtility.isUserAvailableToChangeOtherUserRoleOnCourse(
                course, targetUser, UserCourseRole.HEAD_TEACHER, requestingUser
        ));
    }

    @Test
    void isUserAvailableToChangeOtherUserRoleOnCourse_whenRequestingUserIsNotHigherThanTargetUser_returnsFalse() {
        User requestingUser = createUser();
        User targetUser = createUser();
        Course course = createCourse(List.of(
                createUserCourse(requestingUser, UserCourseRole.TEACHER),
                createUserCourse(targetUser, UserCourseRole.HEAD_TEACHER)
        ));

        assertFalse(CourseUtility.isUserAvailableToChangeOtherUserRoleOnCourse(
                course, targetUser, UserCourseRole.STUDENT, requestingUser
        ));
    }

    @Test
    void isUserAvailableToChangeOtherUserRoleOnCourse_whenRequestingUserNotInCourse_returnsFalse() {
        User requestingUser = createUser();
        User targetUser = createUser();
        User anotherUser = createUser();
        Course course = createCourse(List.of(
                createUserCourse(anotherUser, UserCourseRole.HEAD_TEACHER),
                createUserCourse(targetUser, UserCourseRole.STUDENT)
        ));

        assertFalse(CourseUtility.isUserAvailableToChangeOtherUserRoleOnCourse(
                course, targetUser, UserCourseRole.TEACHER, requestingUser
        ));
    }

    @Test
    void isUserAvailableToChangeOtherUserRoleOnCourse_whenTargetUserNotInCourse_returnsFalse() {
        User requestingUser = createUser();
        User targetUser = createUser();
        Course course = createCourse(List.of(
                createUserCourse(requestingUser, UserCourseRole.HEAD_TEACHER)
        ));

        assertFalse(CourseUtility.isUserAvailableToChangeOtherUserRoleOnCourse(
                course, targetUser, UserCourseRole.TEACHER, requestingUser
        ));
    }

    @Test
    void isUserAvailableToRemoveOtherUserFromCourse_whenRequestingUserIsHigherThanTargetUser_returnsTrue() {
        User requestingUser = createUser();
        User targetUser = createUser();
        Course course = createCourse(List.of(
                createUserCourse(requestingUser, UserCourseRole.HEAD_TEACHER),
                createUserCourse(targetUser, UserCourseRole.STUDENT)
        ));

        assertTrue(CourseUtility.isUserAvailableToRemoveOtherUserFromCourse(
                course, targetUser, requestingUser
        ));
    }

    @Test
    void isUserAvailableToRemoveOtherUserFromCourse_whenRequestingUserIsNotHigherThanTargetUser_returnsFalse() {
        User requestingUser = createUser();
        User targetUser = createUser();
        Course course = createCourse(List.of(
                createUserCourse(requestingUser, UserCourseRole.TEACHER),
                createUserCourse(targetUser, UserCourseRole.HEAD_TEACHER)
        ));

        assertFalse(CourseUtility.isUserAvailableToRemoveOtherUserFromCourse(
                course, targetUser, requestingUser
        ));
    }

    @Test
    void isUserAvailableToRemoveOtherUserFromCourse_whenRequestingUserHasSameRoleAsTargetUser_returnsFalse() {
        User requestingUser = createUser();
        User targetUser = createUser();
        Course course = createCourse(List.of(
                createUserCourse(requestingUser, UserCourseRole.TEACHER),
                createUserCourse(targetUser, UserCourseRole.TEACHER)
        ));

        assertFalse(CourseUtility.isUserAvailableToRemoveOtherUserFromCourse(
                course, targetUser, requestingUser
        ));
    }

    @Test
    void isUserAvailableToRemoveOtherUserFromCourse_whenRequestingUserNotInCourse_returnsFalse() {
        User requestingUser = createUser();
        User targetUser = createUser();
        User anotherUser = createUser();
        Course course = createCourse(List.of(
                createUserCourse(anotherUser, UserCourseRole.HEAD_TEACHER),
                createUserCourse(targetUser, UserCourseRole.STUDENT)
        ));

        assertFalse(CourseUtility.isUserAvailableToRemoveOtherUserFromCourse(
                course, targetUser, requestingUser
        ));
    }

    @Test
    void isUserAvailableToRemoveOtherUserFromCourse_whenTargetUserNotInCourse_returnsFalse() {
        User requestingUser = createUser();
        User targetUser = createUser();
        Course course = createCourse(List.of(
                createUserCourse(requestingUser, UserCourseRole.HEAD_TEACHER)
        ));

        assertFalse(CourseUtility.isUserAvailableToRemoveOtherUserFromCourse(
                course, targetUser, requestingUser
        ));
    }

    private static User createUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        return user;
    }

    private static Course createCourse(List<UserCourse> users) {
        Course course = new Course();
        course.setId(UUID.randomUUID());
        course.setCourseUsers(users);
        return course;
    }

    private static UserCourse createUserCourse(User user, UserCourseRole role) {
        UserCourse userCourse = new UserCourse();
        userCourse.setUser(user);
        userCourse.setUserRole(role);
        return userCourse;
    }
}
