package com.example.hits.application.util;

import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.postcomment.PostComment;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.taskanswercomment.TaskAnswerComment;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskAnswerCommentUtilityTests {

    private User user;
    private Course course;
    private UserCourse userCourse;
    private TaskAnswer taskAnswer;
    private TaskAnswerComment taskAnswerComment;

    @BeforeEach
    public void init () {
        user = createUser();
        userCourse = createUserCourse(user, UserCourseRole.STUDENT);
        course = createCourse(List.of(userCourse));
        userCourse.setCourse(course);
        user.setUserCourses(List.of(userCourse));
        taskAnswer = createTaskAnswer(user, createPost(course, user));
        taskAnswerComment = createTaskAnswerComment(user, taskAnswer);
    }

    @Test
    public void isCommentAvailableForEditing_whenUserIsAuthor_returnsTrue() {
        assertTrue(TaskAnswerCommentUtility.isCommentAvailableForEditing(taskAnswerComment, user));
    }

    @Test
    public void isCommentAvailableForEditing_whenUserIsNotAuthor_returnsFalse() {
        taskAnswerComment.setAuthor(new User());
        assertFalse(TaskAnswerCommentUtility.isCommentAvailableForEditing(taskAnswerComment, user));
    }

    @Test
    public void isTaskAnswerCommentsAvailableForUser_whenUserIsAuthor_returnsTrue() {
        assertTrue(TaskAnswerCommentUtility.isTaskAnswerCommentsAvailableForUser(taskAnswer, user));
    }

    @Test
    public void isTaskAnswerCommentsAvailableForUser_whenUserIsTeacherOnCourse_returnsTrue() {
        taskAnswer.setUser(new User());
        userCourse.setUserRole(UserCourseRole.TEACHER);
        assertTrue(TaskAnswerCommentUtility.isTaskAnswerCommentsAvailableForUser(taskAnswer, user));
    }

    @Test
    public void isTaskAnswerCommentsAvailableForUser_whenUserIsHeadTeacherOnCourse_returnsTrue() {
        taskAnswer.setUser(new User());
        userCourse.setUserRole(UserCourseRole.HEAD_TEACHER);
        assertTrue(TaskAnswerCommentUtility.isTaskAnswerCommentsAvailableForUser(taskAnswer, user));
    }

    @Test
    public void isTaskAnswerCommentsAvailableForUser_whenUserIsStudentAndNotAuthor_returnsFalse() {
        taskAnswer.setUser(new User());
        userCourse.setUserRole(UserCourseRole.STUDENT);
        assertFalse(TaskAnswerCommentUtility.isTaskAnswerCommentsAvailableForUser(taskAnswer, user));
    }

    @Test
    public void isTaskAnswerCommentsAvailableForUser_whenUserIsNotInCourse_returnsFalse() {
        User newUser = new User();
        taskAnswer.setUser(newUser);
        userCourse.setUser(newUser);
        assertFalse(TaskAnswerCommentUtility.isTaskAnswerCommentsAvailableForUser(taskAnswer, user));
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

    private static TaskAnswerComment createTaskAnswerComment(User user, TaskAnswer taskAnswer) {
        TaskAnswerComment taskAnswerComment = new TaskAnswerComment();
        taskAnswerComment.setAuthor(user);
        taskAnswerComment.setTaskAnswer(taskAnswer);
        return taskAnswerComment;
    }

    private static TaskAnswer createTaskAnswer(User user, Post post) {
        TaskAnswer taskAnswer = new TaskAnswer();
        taskAnswer.setUser(user);
        taskAnswer.setPost(post);
        return taskAnswer;
    }

    private static Post createPost(Course course, User user) {
        Post post = new Post();
        post.setAuthor(user);
        post.setCourse(course);
        return post;
    }
}
