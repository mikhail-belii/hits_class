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

public class PostUtilityTests {
    @Test
    void isUserInCourse_whenUserPresentInCourseUsers_returnsTrue() {
        User user = createUser();
        Course course = createCourse(List.of(
                createUserCourse(createUser(), UserCourseRole.STUDENT),
                createUserCourse(user, UserCourseRole.STUDENT)
        ));

        assertTrue(PostUtility.isUserInCourse(course, user));
    }

    @Test
    void isUserInCourse_whenCourseUsersEmpty_returnsFalse() {
        User user = createUser();
        Course course = createCourse(List.of());

        assertFalse(PostUtility.isUserInCourse(course, user));
    }

    @Test
    void isUserInCourse_whenUserNotPresent_returnsFalse() {
        User user = createUser();
        Course course = createCourse(List.of(
                createUserCourse(createUser(), UserCourseRole.STUDENT),
                createUserCourse(createUser(), UserCourseRole.TEACHER)
        ));

        assertFalse(PostUtility.isUserInCourse(course, user));
    }

    @Test
    void isAvailableForEditing_whenUserNotInCourse_returnsFalse() {
        User user = createUser();
        Course course = createCourse(List.of(
                createUserCourse(createUser(), UserCourseRole.HEAD_TEACHER)
        ));

        assertFalse(PostUtility.isAvailableForEditing(course, user));
    }

    @Test
    void isAvailableForEditing_whenUserRoleIsStudent_returnsFalse() {
        User user = createUser();
        Course course = createCourse(List.of(
                createUserCourse(user, UserCourseRole.STUDENT)
        ));

        assertFalse(PostUtility.isAvailableForEditing(course, user));
    }

    @Test
    void isAvailableForEditing_whenUserRoleIsTeacher_returnsTrue() {
        User user = createUser();
        Course course = createCourse(List.of(
                createUserCourse(user, UserCourseRole.TEACHER)
        ));

        assertTrue(PostUtility.isAvailableForEditing(course, user));
    }

    @Test
    void isAvailableForEditing_whenUserRoleIsHeadTeacher_returnsTrue() {
        User user = createUser();
        Course course = createCourse(List.of(
                createUserCourse(user, UserCourseRole.HEAD_TEACHER)
        ));

        assertTrue(PostUtility.isAvailableForEditing(course, user));
    }

    @Test
    void isPostAvailableForReading_whenUserInCoursePostBelongsToCourse_returnsTrue() {
        User user = createUser();
        Course course = createCourse(List.of(
                createUserCourse(user, UserCourseRole.STUDENT)
        ));
        Post post = createPost(course);

        assertTrue(PostUtility.isPostAvailableForReading(course, post, user));
    }

    @Test
    void isPostAvailableForReading_whenUserNotInCoursePostBelongsToCourse_returnsFalse() {
        User user = createUser();
        Course course = createCourse(List.of(
                createUserCourse(createUser(), UserCourseRole.STUDENT)
        ));
        Post post = createPost(course);

        assertFalse(PostUtility.isPostAvailableForReading(course, post, user));
    }

    @Test
    void isPostAvailableForReading_whenPostBelongsToAnotherCourseUserInCourse_returnsFalse() {
        User user = createUser();

        Course course = createCourse(List.of(createUserCourse(user, UserCourseRole.STUDENT)));
        Course anotherCourse = createCourse(List.of(createUserCourse(user, UserCourseRole.STUDENT)));

        Post post = createPost(anotherCourse);

        assertFalse(PostUtility.isPostAvailableForReading(course, post, user));
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
        userCourse.setId(UUID.randomUUID());
        userCourse.setUser(user);
        userCourse.setUserRole(role);
        return userCourse;
    }

    private static Post createPost(Course course) {
        Post post = new Post();
        post.setId(UUID.randomUUID());
        post.setCourse(course);
        return post;
    }
}
