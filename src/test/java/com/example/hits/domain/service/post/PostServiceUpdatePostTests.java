package com.example.hits.domain.service.post;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.model.post.PostUpdateModel;
import com.example.hits.application.repository.CourseRepository;
import com.example.hits.application.repository.PostRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.domain.entity.course.Course;
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

    @InjectMocks
    private PostService postService;

    @Test
    void updatePost_validTeacher_updatesTextAndSavesPost() throws ExceptionWrapper {
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
