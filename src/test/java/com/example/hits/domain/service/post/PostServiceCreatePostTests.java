package com.example.hits.domain.service.post;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.model.common.IdResponseModel;
import com.example.hits.application.model.file.FileModel;
import com.example.hits.application.model.post.PostCreateModel;
import com.example.hits.application.repository.CourseRepository;
import com.example.hits.application.repository.FileRepository;
import com.example.hits.application.repository.PostRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.domain.entity.file.File;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.service.taskanswer.TaskAnswerGeneralService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PostServiceCreatePostTests {
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FileRepository fileRepository;
    @Mock
    private TaskAnswerGeneralService taskAnswerGeneralService;

    @InjectMocks
    private PostService postService;

    @Test
    void createPost_validTeacher_savesPostAndReturnsId() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = createUser(userId);
        Course course = createCourseWithUserRole(user, UserCourseRole.TEACHER);
        PostCreateModel postCreateModel = new PostCreateModel(
                "Текст поста",
                List.of(),
                PostType.TASK,
                100
        );

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IdResponseModel response = postService.createPost(courseId, userId, postCreateModel);
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

        verify(postRepository).save(postCaptor.capture());
        Post savedPost = postCaptor.getValue();
        verify(taskAnswerGeneralService).createTaskAnswerForEveryCourseMember(course, savedPost);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getId());
        Assertions.assertEquals(savedPost.getId(), response.getId());
        Assertions.assertEquals("Текст поста", savedPost.getText());
        Assertions.assertEquals(PostType.TASK, savedPost.getPostType());
        Assertions.assertEquals(100, savedPost.getMaxScore());
        Assertions.assertEquals(course, savedPost.getCourse());
        Assertions.assertEquals(user, savedPost.getAuthor());
        Assertions.assertNotNull(savedPost.getCreatedAt());
    }

    @Test
    void createPost_withFilesAndNonTaskType_createsFilesAndDoesNotCreateTaskAnswers() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID firstFileId = UUID.randomUUID();
        UUID secondFileId = UUID.randomUUID();

        User teacher = createUser(userId);
        Course course = createCourseWithUserRole(teacher, UserCourseRole.TEACHER);
        PostCreateModel postCreateModel = new PostCreateModel(
                "text",
                List.of(new FileModel(firstFileId, "name"), new FileModel(secondFileId, "name")),
                PostType.ANNOUNCEMENT,
                null
        );

        File firstFile = new File().setId(firstFileId).setUploader(teacher).setPath("files/uploads/a.txt").setOriginalName("a.txt").setCreatedAt(LocalDateTime.now());
        File secondFile = new File().setId(secondFileId).setUploader(teacher).setPath("files/uploads/b.txt").setOriginalName("b.txt").setCreatedAt(LocalDateTime.now());

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(teacher));
        when(fileRepository.findAllById(List.of(firstFileId, secondFileId))).thenReturn(List.of(firstFile, secondFile));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        postService.createPost(courseId, userId, postCreateModel);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());

        Post savedPost = postCaptor.getValue();
        verify(taskAnswerGeneralService, never()).createTaskAnswerForEveryCourseMember(any(Course.class), any(Post.class));
        Assertions.assertNotNull(savedPost.getFiles());
        Assertions.assertEquals(2, savedPost.getFiles().size());
        Assertions.assertEquals(firstFileId, savedPost.getFiles().get(0).getId());
        Assertions.assertEquals(secondFileId, savedPost.getFiles().get(1).getId());
        Assertions.assertEquals(savedPost, savedPost.getFiles().get(0).getPost());
        Assertions.assertEquals(savedPost, savedPost.getFiles().get(1).getPost());
    }

    @Test
    void createPost_courseNotFound_throwsEntityNotFoundException() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PostCreateModel postCreateModel = new PostCreateModel("текст", List.of(), PostType.ANNOUNCEMENT, null);
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.createPost(courseId, userId, postCreateModel)
        );
        Assertions.assertEquals(EntityNotFoundException.class, exception.getExceptionClass());
        Assertions.assertEquals("Cannot find course with requested id", exception.getErrors().get("courseId"));

        verifyNoInteractions(userRepository, postRepository, taskAnswerGeneralService);
    }

    @Test
    void createPost_userNotFound_throwsEntityNotFoundException() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Course course = createCourseWithUserRole(createUser(UUID.randomUUID()), UserCourseRole.TEACHER);
        PostCreateModel postCreateModel = new PostCreateModel("текст", List.of(), PostType.ANNOUNCEMENT, null);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.createPost(courseId, userId, postCreateModel)
        );
        Assertions.assertEquals(EntityNotFoundException.class, exception.getExceptionClass());
        Assertions.assertEquals("User not found", exception.getErrors().get("userId"));

        verifyNoInteractions(postRepository, taskAnswerGeneralService);
    }

    @Test
    void createPost_studentUser_throwsForbiddenStatusException() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User student = createUser(userId);
        Course course = createCourseWithUserRole(student, UserCourseRole.STUDENT);
        PostCreateModel postCreateModel = new PostCreateModel("текст", List.of(), PostType.ANNOUNCEMENT, null);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(student));

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.createPost(courseId, userId, postCreateModel)
        );
        Assertions.assertEquals(ResponseStatusException.class, exception.getExceptionClass());
        Assertions.assertEquals("User has no rights to this action", exception.getErrors().get("forbidden"));
        verifyNoInteractions(postRepository, taskAnswerGeneralService);
    }
}
