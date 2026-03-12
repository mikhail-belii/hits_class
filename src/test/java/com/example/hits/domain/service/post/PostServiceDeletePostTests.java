package com.example.hits.domain.service.post;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.repository.CourseRepository;
import com.example.hits.application.repository.FileRepository;
import com.example.hits.application.repository.PostRepository;
import com.example.hits.application.repository.PostCommentRepository;
import com.example.hits.application.repository.TaskAnswerCommentRepository;
import com.example.hits.application.repository.TaskAnswerRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.post.PostType;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.hits.domain.service.post.PostServiceTestUtils.createCourseWithUserRole;
import static com.example.hits.domain.service.post.PostServiceTestUtils.createUser;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceDeletePostTests {
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FileRepository fileRepository;
    @Mock
    private PostCommentRepository postCommentRepository;
    @Mock
    private TaskAnswerRepository taskAnswerRepository;
    @Mock
    private TaskAnswerCommentRepository taskAnswerCommentRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void deletePost_validTeacherAndPostInCourse_deletesPost() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        User teacher = createUser(userId);
        Course course = createCourseWithUserRole(teacher, UserCourseRole.TEACHER);
        course.setId(courseId);
        Post post = createPost(postId, course, teacher);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(teacher));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(taskAnswerRepository.findAllByPostId(postId)).thenReturn(List.of());

        postService.deletePost(courseId, postId, userId);

        verify(postRepository).delete(post);
    }

    @Test
    void deletePost_courseNotFound_throwsEntityNotFoundException() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.deletePost(courseId, postId, userId)
        );

        Assertions.assertEquals(EntityNotFoundException.class, exception.getExceptionClass());
        Assertions.assertEquals("Cannot find course with requested id", exception.getErrors().get("courseId"));
        verifyNoInteractions(userRepository, postRepository);
    }

    @Test
    void deletePost_userNotFound_throwsEntityNotFoundException() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        User anotherUser = createUser(UUID.randomUUID());
        Course course = createCourseWithUserRole(anotherUser, UserCourseRole.TEACHER);
        course.setId(courseId);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.deletePost(courseId, postId, userId)
        );

        Assertions.assertEquals(EntityNotFoundException.class, exception.getExceptionClass());
        Assertions.assertEquals("User not found", exception.getErrors().get("userId"));
        verifyNoInteractions(postRepository);
    }

    @Test
    void deletePost_postNotFound_throwsEntityNotFoundException() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        User teacher = createUser(userId);
        Course course = createCourseWithUserRole(teacher, UserCourseRole.TEACHER);
        course.setId(courseId);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(teacher));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.deletePost(courseId, postId, userId)
        );

        Assertions.assertEquals(EntityNotFoundException.class, exception.getExceptionClass());
        Assertions.assertEquals("Post not found", exception.getErrors().get("postId"));
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    void deletePost_postIsFromAnotherCourse_throwsBadRequestException() {
        UUID courseId = UUID.randomUUID();
        UUID anotherCourseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        User teacher = createUser(userId);
        Course course = createCourseWithUserRole(teacher, UserCourseRole.TEACHER);
        course.setId(courseId);
        Course anotherCourse = createCourseWithUserRole(createUser(UUID.randomUUID()), UserCourseRole.STUDENT);
        anotherCourse.setId(anotherCourseId);
        Post post = createPost(postId, anotherCourse, teacher);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(teacher));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.deletePost(courseId, postId, userId)
        );

        Assertions.assertEquals(BadRequestException.class, exception.getExceptionClass());
        Assertions.assertEquals("You can't delete this post", exception.getErrors().get("Bad request"));
        verify(postRepository, never()).delete(any(Post.class));
    }

    private static Post createPost(UUID id, Course course, User author) {
        return new Post()
                .setId(id)
                .setCourse(course)
                .setAuthor(author)
                .setText("text")
                .setPostType(PostType.ANNOUNCEMENT)
                .setCreatedAt(LocalDateTime.now());
    }
}
