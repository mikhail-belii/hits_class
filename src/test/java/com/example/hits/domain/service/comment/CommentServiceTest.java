package com.example.hits.domain.service.comment;

import com.example.hits.application.model.comment.postcomment.PostCommentCreateModel;
import com.example.hits.application.model.comment.postcomment.PostCommentEditModel;
import com.example.hits.application.model.comment.postcomment.PostCommentModel;
import com.example.hits.application.repository.*;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.postcomment.PostComment;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.example.hits.domain.service.comment.CommentServiceTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TaskAnswerCommentRepository taskAnswerCommentRepository;
    @Mock
    private PostCommentRepository postCommentRepository;
    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User user;
    private Course course;
    private UserCourse userCourse;
    private Post post;
    private PostComment postComment;
    private PostCommentCreateModel postCommentCreateModel;
    private PostCommentEditModel postCommentEditModel;

    @BeforeEach
    public void init () {
        user = createUser();
        course = createCourse("Course Name", "Course Desc", false);
        userCourse = createUserCourse(user, course, UserCourseRole.HEAD_TEACHER);
        user.setUserCourses(List.of(userCourse));
        course.setCourseUsers(List.of(userCourse));
        post = createPost(course, user);
        postComment = createPostComment(user, post);
        post.setComments(List.of(postComment));
        postCommentCreateModel = createPostCommentCreateModel("testComment");
        postCommentEditModel = createPostCommentEditModel("editedComment");
    }

    @Test
    void getPostComments_whenCanGetComments_returnsComments() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        List<PostCommentModel> result = commentService.getPostComments(user.getId(), post.getId());

        PostCommentModel postCommentModel = result.get(0);
        assertEquals(postCommentModel.getId(), postComment.getId());
    }

    @Test
    void getPostComments_whenUserNotInCourse_throwsForbiddenRightsException() {
        User newUser = new User();
        userCourse.setUser(newUser);
        postComment.setAuthor(newUser);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        assertThrows(ExceptionUtility.forbiddenRightsException().getClass(),
                () -> commentService.getPostComments(user.getId(), post.getId()));
    }

    @Test
    void getPostComments_whenUserNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.userNotFoundException().getClass(),
                () -> commentService.getPostComments(user.getId(), post.getId()));
    }

    @Test
    void getPostComments_whenPostNotFound_throwsPostNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(postRepository.findById(post.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.postNotFoundException().getClass(),
                () -> commentService.getPostComments(user.getId(), post.getId()));
    }

    @Test
    void createPostComment_whenCanCreateComment_commentCreated() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        PostCommentModel result = commentService.createPostComment(user.getId(), post.getId(), postCommentCreateModel);

        assertEquals(result.getText(), postCommentCreateModel.getText());
        assertEquals(result.getAuthor().getId(), user.getId());

        verify(postCommentRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void createPostComment_whenUserNotInCourse_throwsForbiddenRightsException() {
        User newUser = new User();
        userCourse.setUser(newUser);
        postComment.setAuthor(newUser);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        assertThrows(ExceptionUtility.forbiddenRightsException().getClass(),
                () -> commentService.createPostComment(user.getId(), post.getId(), postCommentCreateModel));

        verify(postCommentRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void createPostComment_whenUserNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.userNotFoundException().getClass(),
                () -> commentService.createPostComment(user.getId(), post.getId(), postCommentCreateModel));

        verify(postCommentRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void createPostComment_whenPostNotFound_throwsPostNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(postRepository.findById(post.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.postNotFoundException().getClass(),
                () -> commentService.createPostComment(user.getId(), post.getId(), postCommentCreateModel));

        verify(postCommentRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void editPostComment_whenCanEditComment_commentEdited() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(postCommentRepository.findById(postComment.getId())).thenReturn(Optional.of(postComment));

        PostCommentModel result = commentService.editPostComment(user.getId(), postComment.getId(), postCommentEditModel);

        assertEquals(result.getText(), postCommentEditModel.getText());
        assertEquals(result.getAuthor().getId(), user.getId());

        verify(postCommentRepository, times(1)).flush();
    }

    @Test
    void editPostComment_whenUserNotInCourse_throwsForbiddenRightsException() {
        User newUser = new User();
        userCourse.setUser(newUser);
        postComment.setAuthor(newUser);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(postCommentRepository.findById(postComment.getId())).thenReturn(Optional.of(postComment));

        assertThrows(ExceptionUtility.forbiddenRightsException().getClass(),
                () -> commentService.editPostComment(user.getId(), postComment.getId(), postCommentEditModel));

        verify(postCommentRepository, times(0)).flush();
    }

    @Test
    void editPostComment_whenUserNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.userNotFoundException().getClass(),
                () -> commentService.editPostComment(user.getId(), postComment.getId(), postCommentEditModel));

        verify(postCommentRepository, times(0)).flush();
    }

    @Test
    void editPostComment_whenPostCommentNotFound_throwsPostNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(postCommentRepository.findById(postComment.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.postCommentNotFoundException().getClass(),
                () -> commentService.editPostComment(user.getId(), postComment.getId(), postCommentEditModel));

        verify(postCommentRepository, times(0)).flush();
    }

}
