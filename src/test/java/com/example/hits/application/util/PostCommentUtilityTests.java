package com.example.hits.application.util;

import com.example.hits.application.model.course.CourseCreateModel;
import com.example.hits.application.model.course.CourseEditModel;
import com.example.hits.application.repository.CourseRepository;
import com.example.hits.application.repository.UserCourseRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.postcomment.PostComment;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import com.example.hits.domain.service.course.CourseCodeGenerator;
import com.example.hits.domain.service.course.CourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.example.hits.domain.service.course.CourseServiceTestUtils.*;
import static com.example.hits.domain.service.course.CourseServiceTestUtils.createCourseCreateModel;
import static com.example.hits.domain.service.course.CourseServiceTestUtils.createCourseEditModel;
import static com.example.hits.domain.service.course.CourseServiceTestUtils.createUserCourse;
import static org.junit.jupiter.api.Assertions.*;

public class PostCommentUtilityTests {

    private User user;
    private Course course;
    private UserCourse userCourse;
    private PostComment postComment;

    @BeforeEach
    public void init () {
        user = createUser();
        userCourse = createUserCourse(user, UserCourseRole.STUDENT);
        course = createCourse(List.of(userCourse));
        userCourse.setCourse(course);
        user.setUserCourses(List.of(userCourse));
        postComment = createPostComment(user, createPost(course, user));
    }

    @Test
    public void isCommentAvailableForEditing_whenUserIsAuthor_returnsTrue() {
        assertTrue(PostCommentUtility.isCommentAvailableForEditing(postComment, user));
    }

    @Test
    public void isCommentAvailableForEditing_whenUserIsNotAuthor_returnsFalse() {
        postComment.setAuthor(new User());
        assertFalse(PostCommentUtility.isCommentAvailableForEditing(postComment, user));
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

    private static PostComment createPostComment(User user, Post post) {
        PostComment postComment = new PostComment();
        postComment.setAuthor(user);
        postComment.setPost(post);
        return postComment;
    }

    private static Post createPost(Course course, User user) {
        Post post = new Post();
        post.setAuthor(user);
        post.setCourse(course);
        return post;
    }
}
