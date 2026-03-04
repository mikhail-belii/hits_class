package com.example.hits.domain.service.post;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.hits.domain.service.post.PostServiceTestUtils.createCourseWithUserRole;
import static com.example.hits.domain.service.post.PostServiceTestUtils.createUser;
import static org.mockito.Mockito.verify;
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
        post.setAttachments(List.of(oldAttachment));

        File newFile = new File().setId(newFileId).setUploader(teacher).setPath("files/uploads/new.txt").setOriginalName("new.txt").setCreatedAt(LocalDateTime.now());
        PostUpdateModel model = new PostUpdateModel("new-text", List.of(new FileModel(newFileId)));

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(userId)).thenReturn(Optional.of(teacher));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(fileRepository.findAllById(List.of(newFileId))).thenReturn(List.of(newFile));
        when(attachmentRepository.existsByFile_IdAndPost_IdNot(newFileId, postId)).thenReturn(false);

        postService.updatePost(courseId, postId, userId, model);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());

        Post savedPost = postCaptor.getValue();
        Assertions.assertEquals("new-text", savedPost.getText());
        Assertions.assertNotNull(savedPost.getAttachments());
        Assertions.assertEquals(1, savedPost.getAttachments().size());
        Assertions.assertEquals(newFileId, savedPost.getAttachments().get(0).getFile().getId());
        Assertions.assertEquals(savedPost, savedPost.getAttachments().get(0).getPost());
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
