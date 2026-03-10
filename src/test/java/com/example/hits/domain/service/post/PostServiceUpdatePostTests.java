package com.example.hits.domain.service.post;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.model.file.FileModel;
import com.example.hits.application.repository.AttachmentRepository;
import com.example.hits.application.model.post.PostUpdateModel;
import com.example.hits.application.repository.CourseRepository;
import com.example.hits.application.repository.FileRepository;
import com.example.hits.application.repository.PostRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.domain.entity.attachment.Attachment;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.file.File;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import jakarta.persistence.EntityNotFoundException;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

import static com.example.hits.domain.service.post.PostServiceTestUtils.createCourseWithUserRole;
import static com.example.hits.domain.service.post.PostServiceTestUtils.createUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PostServiceUpdatePostTests {
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FileRepository fileRepository;
    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void updatePost_validTeacher_updatesTextAndSavesPost() {
        UUID courseId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User teacher = createUser(userId);
        Course course = createCourseWithUserRole(teacher, UserCourseRole.TEACHER);
        course.setId(courseId);
        Post post = createPost(postId, course, teacher, "фуфу");

        PostUpdateModel model = new PostUpdateModel("новый текст", List.of());

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(teacher));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postService.updatePost(courseId, postId, userId, model);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());

        Post savedPost = postCaptor.getValue();
        Assertions.assertEquals("новый текст", savedPost.getText());
        Assertions.assertNotNull(savedPost.getUpdatedAt());
        Assertions.assertEquals(postId, savedPost.getId());
    }

    @Test
    void updatePost_withFiles_replacesAttachments() {
        UUID courseId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID newFileId = UUID.randomUUID();

        User teacher = createUser(userId);
        Course course = createCourseWithUserRole(teacher, UserCourseRole.TEACHER);
        course.setId(courseId);
        Post post = createPost(postId, course, teacher, "old-text");

        File oldFile = new File().setId(UUID.randomUUID()).setUploader(teacher).setPath("files/uploads/old.txt").setOriginalName("old.txt").setCreatedAt(LocalDateTime.now());
        Attachment oldAttachment = new Attachment().setFile(oldFile).setPost(post).setCreatedAt(LocalDateTime.now());
        post.setAttachments(new ArrayList<>(Arrays.asList(oldAttachment)));

        File newFile = new File().setId(newFileId).setUploader(teacher).setPath("files/uploads/new.txt").setOriginalName("new.txt").setCreatedAt(LocalDateTime.now());
        PostUpdateModel model = new PostUpdateModel("new-text", List.of(new FileModel(newFileId)));

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(teacher));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(fileRepository.findAllById(List.of(newFileId))).thenReturn(List.of(newFile));

        postService.updatePost(courseId, postId, userId, model);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());

        Post savedPost = postCaptor.getValue();
        Assertions.assertEquals("new-text", savedPost.getText());
        Assertions.assertNotNull(savedPost.getAttachments());
        Assertions.assertEquals(1, savedPost.getAttachments().size());
        Assertions.assertEquals(newFileId, savedPost.getAttachments().getFirst().getFile().getId());
        Assertions.assertEquals(savedPost, savedPost.getAttachments().getFirst().getPost());
    }

    @Test
    void updatePost_courseNotFound_throwsEntityNotFoundException() {
        UUID courseId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PostUpdateModel model = new PostUpdateModel("text", List.of());

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.updatePost(courseId, postId, userId, model)
        );

        Assertions.assertEquals(EntityNotFoundException.class, exception.getExceptionClass());
        Assertions.assertEquals("Cannot find course with requested id", exception.getErrors().get("courseId"));
        verifyNoInteractions(userRepository, postRepository);
    }

    @Test
    void updatePost_userNotFound_throwsEntityNotFoundException() {
        UUID courseId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User anotherUser = createUser(UUID.randomUUID());
        Course course = createCourseWithUserRole(anotherUser, UserCourseRole.TEACHER);
        course.setId(courseId);
        PostUpdateModel model = new PostUpdateModel("текст", List.of());

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.updatePost(courseId, postId, userId, model)
        );

        Assertions.assertEquals(EntityNotFoundException.class, exception.getExceptionClass());
        Assertions.assertEquals("User not found", exception.getErrors().get("userId"));
        verifyNoInteractions(postRepository);
    }

    @Test
    void updatePost_postNotFound_throwsEntityNotFoundException() {
        UUID courseId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User teacher = createUser(userId);
        Course course = createCourseWithUserRole(teacher, UserCourseRole.TEACHER);
        course.setId(courseId);
        PostUpdateModel model = new PostUpdateModel("text", List.of());

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(teacher));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.updatePost(courseId, postId, userId, model)
        );

        Assertions.assertEquals(EntityNotFoundException.class, exception.getExceptionClass());
        Assertions.assertEquals("Post not found", exception.getErrors().get("postId"));
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void updatePost_userHasNoTeacherRole_throwsForbiddenStatusException() {
        UUID courseId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User student = createUser(userId);
        Course course = createCourseWithUserRole(student, UserCourseRole.STUDENT);
        course.setId(courseId);
        Post post = createPost(postId, course, student, "old");
        PostUpdateModel model = new PostUpdateModel("new", List.of());

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(student));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.updatePost(courseId, postId, userId, model)
        );

        Assertions.assertEquals(ResponseStatusException.class, exception.getExceptionClass());
        Assertions.assertEquals("User has no rights to this action", exception.getErrors().get("forbidden"));
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void updatePost_postIsFromAnotherCourse_throwsBadRequestException() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        User teacher = createUser(userId);
        Course course = createCourseWithUserRole(teacher, UserCourseRole.TEACHER);
        course.setId(courseId);
        Course anotherCourse = createCourseWithUserRole(createUser(UUID.randomUUID()), UserCourseRole.STUDENT);
        anotherCourse.setId(UUID.randomUUID());
        Post post = createPost(postId, anotherCourse, teacher, "old");
        PostUpdateModel model = new PostUpdateModel("new", List.of());

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(teacher));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.updatePost(courseId, postId, userId, model)
        );

        Assertions.assertEquals(BadRequestException.class, exception.getExceptionClass());
        Assertions.assertEquals("You can't edit this post", exception.getErrors().get("Bad request"));
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void updatePost_fileNotFound_throwsBadRequestException() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        User teacher = createUser(userId);
        Course course = createCourseWithUserRole(teacher, UserCourseRole.TEACHER);
        course.setId(courseId);
        Post post = createPost(postId, course, teacher, "old");
        PostUpdateModel model = new PostUpdateModel("new", List.of(new FileModel(fileId)));

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(teacher));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(fileRepository.findAllById(List.of(fileId))).thenReturn(List.of());

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.updatePost(courseId, postId, userId, model)
        );

        Assertions.assertEquals(BadRequestException.class, exception.getExceptionClass());
        Assertions.assertEquals("One or more files not found", exception.getErrors().get("Bad request"));
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void updatePost_fileUploadedByAnotherUser_throwsBadRequestException() {
        UUID courseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        User teacher = createUser(userId);
        User anotherUser = createUser(UUID.randomUUID());
        Course course = createCourseWithUserRole(teacher, UserCourseRole.TEACHER);
        course.setId(courseId);
        Post post = createPost(postId, course, teacher, "old");
        File foreignFile = new File().setId(fileId).setUploader(anotherUser).setPath("files/uploads/foreign.txt").setOriginalName("foreign.txt").setCreatedAt(LocalDateTime.now());
        PostUpdateModel model = new PostUpdateModel("new", List.of(new FileModel(fileId)));

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(teacher));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(fileRepository.findAllById(List.of(fileId))).thenReturn(List.of(foreignFile));

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> postService.updatePost(courseId, postId, userId, model)
        );

        Assertions.assertEquals(BadRequestException.class, exception.getExceptionClass());
        Assertions.assertEquals("You can attach only your files", exception.getErrors().get("Bad request"));
        verify(postRepository, never()).save(any(Post.class));
    }

    private static Post createPost(UUID id, Course course, User author, String text) {
        return new Post()
                .setId(id)
                .setCourse(course)
                .setAuthor(author)
                .setText(text)
                .setPostType(PostType.ANNOUNCEMENT)
                .setCreatedAt(LocalDateTime.now());
    }
}
