package com.example.hits.domain.service.post;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.model.post.PostFullModel;
import com.example.hits.application.model.post.PostShortModel;
import com.example.hits.application.repository.CourseRepository;
import com.example.hits.application.repository.PostRepository;
import com.example.hits.application.repository.TaskAnswerRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import jakarta.persistence.EntityNotFoundException;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.hits.domain.service.post.PostServiceTestUtils.createCourseWithUserRole;
import static com.example.hits.domain.service.post.PostServiceTestUtils.createUser;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PostServiceGetPostsTests {
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskAnswerRepository taskAnswerRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void getClassPosts_validUserInCourse_returnsOnlyCoursePostsAsModels() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = createUser(userId);
        Course course = createCourseWithUserRole(user, UserCourseRole.STUDENT);
        Course anotherCourse = createCourseWithUserRole(createUser(UUID.randomUUID()), UserCourseRole.STUDENT);
        course.setId(courseId);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Post olderPost = createPost(UUID.randomUUID(), course, user, "post-1");
        olderPost.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        Post newerPost = createPost(UUID.randomUUID(), course, user, "post-2");
        newerPost.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 5));
        when(postRepository.findAll()).thenReturn(List.of(
                olderPost,
                newerPost,
                createPost(UUID.randomUUID(), anotherCourse, user, "post-3")
        ));

        List<PostShortModel> posts = postService.getClassPosts(courseId, userId);

        Assertions.assertEquals(2, posts.size());
        Assertions.assertEquals("post-2", posts.get(0).getText());
        Assertions.assertEquals("post-1", posts.get(1).getText());
    }

    @Test
    void getClassPosts_courseNotFound_throwsEntityNotFoundException() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.getClassPosts(courseId, userId)
        );

        Assertions.assertEquals(EntityNotFoundException.class, exception.getExceptionClass());
        Assertions.assertEquals("Cannot find course with requested id", exception.getErrors().get("courseId"));
        verifyNoInteractions(userRepository, postRepository);
    }

    @Test
    void getClassPosts_userNotFound_throwsEntityNotFoundException() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = createUser(UUID.randomUUID());
        Course course = createCourseWithUserRole(user, UserCourseRole.TEACHER);
        course.setId(courseId);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.getClassPosts(courseId, userId)
        );

        Assertions.assertEquals(EntityNotFoundException.class, exception.getExceptionClass());
        Assertions.assertEquals("User not found", exception.getErrors().get("userId"));
        verifyNoInteractions(postRepository);
    }

    @Test
    void getClassPosts_userNotInCourse_throwsForbiddenStatusException() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User anotherUser = createUser(UUID.randomUUID());
        Course course = createCourseWithUserRole(anotherUser, UserCourseRole.STUDENT);
        course.setId(courseId);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(createUser(userId)));

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.getClassPosts(courseId, userId)
        );

        Assertions.assertEquals(ResponseStatusException.class, exception.getExceptionClass());
        Assertions.assertEquals("User has no rights to this action", exception.getErrors().get("forbidden"));
        verifyNoInteractions(postRepository);
    }

    @Test
    void getPostInfo_validUserAndPost_returnsPostFullModelWithTaskAnswer() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID taskAnswerId = UUID.randomUUID();

        User user = createUser(userId);
        Course course = createCourseWithUserRole(user, UserCourseRole.STUDENT);
        course.setId(courseId);
        Post post = createPost(postId, course, user, "Р‘РµР±РµР±Рµ");
        TaskAnswer taskAnswer = new TaskAnswer().setId(taskAnswerId).setUser(user).setPost(post);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(taskAnswerRepository.findByUserIdAndPostId(userId, postId)).thenReturn(Optional.of(taskAnswer));

        PostFullModel postFullModel = postService.getPostInfo(courseId, postId, userId);

        Assertions.assertEquals(postId, postFullModel.getId());
        Assertions.assertEquals("Р‘РµР±РµР±Рµ", postFullModel.getText());
        Assertions.assertNotNull(postFullModel.getTaskAnswer());
        Assertions.assertEquals(taskAnswerId, postFullModel.getTaskAnswer().getId());
        verify(taskAnswerRepository).findByUserIdAndPostId(userId, postId);
    }

    @Test
    void getPostInfo_whenTaskAnswerIsMissing_returnsPostFullModelWithNullTaskAnswer() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        User user = createUser(userId);
        Course course = createCourseWithUserRole(user, UserCourseRole.STUDENT);
        course.setId(courseId);
        Post post = createPost(postId, course, user, "Р’СЃРµРј РїСЂРёРІРµС‚");

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(taskAnswerRepository.findByUserIdAndPostId(userId, postId)).thenReturn(Optional.empty());

        PostFullModel postFullModel = postService.getPostInfo(courseId, postId, userId);

        Assertions.assertEquals(postId, postFullModel.getId());
        Assertions.assertNull(postFullModel.getTaskAnswer());
        verify(taskAnswerRepository).findByUserIdAndPostId(userId, postId);
    }

    @Test
    void getPostInfo_postNotFound_throwsEntityNotFoundException() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        User user = createUser(userId);
        Course course = createCourseWithUserRole(user, UserCourseRole.STUDENT);
        course.setId(courseId);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.getPostInfo(courseId, postId, userId)
        );

        Assertions.assertEquals(EntityNotFoundException.class, exception.getExceptionClass());
        Assertions.assertEquals("Post not found", exception.getErrors().get("postId"));
        verifyNoInteractions(taskAnswerRepository);
    }

    @Test
    void getPostInfo_userHasNoAccess_throwsForbiddenStatusException() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        User user = createUser(userId);
        User anotherUser = createUser(UUID.randomUUID());

        Course course = createCourseWithUserRole(anotherUser, UserCourseRole.STUDENT);
        course.setId(courseId);
        Post post = createPost(postId, course, anotherUser, "Р’СЃРµРј РїСЂРёРІРµС‚");

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.getPostInfo(courseId, postId, userId)
        );

        Assertions.assertEquals(BadRequestException.class, exception.getExceptionClass());
        verifyNoInteractions(taskAnswerRepository);
    }

    private static Post createPost(UUID postId, Course course, User author, String text) {
        Post post = new Post();
        post.setId(postId);
        post.setCourse(course);
        post.setAuthor(author);
        post.setText(text);
        post.setPostType(PostType.ANNOUNCEMENT);
        post.setCreatedAt(LocalDateTime.now());
        return post;
    }
}
