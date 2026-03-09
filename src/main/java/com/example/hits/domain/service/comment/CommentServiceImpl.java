package com.example.hits.domain.service.comment;

import com.example.hits.application.model.comment.postcomment.PostCommentCreateModel;
import com.example.hits.application.model.comment.postcomment.PostCommentEditModel;
import com.example.hits.application.model.comment.postcomment.PostCommentModel;
import com.example.hits.application.model.comment.taskanswercomment.TaskAnswerCommentCreateModel;
import com.example.hits.application.model.comment.taskanswercomment.TaskAnswerCommentEditModel;
import com.example.hits.application.model.comment.taskanswercomment.TaskAnswerCommentModel;
import com.example.hits.application.model.course.*;
import com.example.hits.application.repository.*;
import com.example.hits.application.service.CommentService;
import com.example.hits.application.service.CourseService;
import com.example.hits.application.util.CourseUtility;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.application.util.PostCommentUtility;
import com.example.hits.application.util.PostUtility;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.postcomment.PostComment;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.taskanswercomment.TaskAnswerComment;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import com.example.hits.domain.mapper.CourseMapper;
import com.example.hits.domain.mapper.PostCommentMapper;
import com.example.hits.domain.mapper.UserCourseMapper;
import com.example.hits.domain.service.course.CourseCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final UserRepository userRepository;

    private final TaskAnswerCommentRepository taskAnswerCommentRepository;

    private final PostCommentRepository postCommentRepository;

    private final PostRepository postRepository;

    private final TaskAnswerRepository taskAnswerRepository;

    public List<PostCommentModel> getPostComments(UUID requestingUserId, UUID postId) {
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        Post post = postRepository.findById(postId)
                .orElseThrow(ExceptionUtility::postNotFoundException);
        if (!PostUtility.isPostAvailableForReading(post.getCourse(), post, requestingUser)) {
            throw ExceptionUtility.forbiddenRightsException();
        }
        return post.getComments()
                .stream()
                .map(PostCommentMapper::toModel)
                .toList();
    }

    public PostCommentModel createPostComment(
            UUID requestingUserId,
            UUID postId,
            PostCommentCreateModel postCommentCreateModel
    ) {
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        Post post = postRepository.findById(postId)
                .orElseThrow(ExceptionUtility::postNotFoundException);
        if (!PostUtility.isPostAvailableForReading(post.getCourse(), post, requestingUser)) {
            throw ExceptionUtility.forbiddenRightsException();
        }
        PostComment postComment = createPostComment(postCommentCreateModel, requestingUser, post);
        postCommentRepository.saveAndFlush(postComment);
        return PostCommentMapper.toModel(postComment);
    }

    public PostCommentModel editPostComment(
            UUID requestingUserId,
            UUID postCommentId,
            PostCommentEditModel postCommentEditModel
    ) {
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        PostComment postComment = postCommentRepository.findById(postCommentId)
                .orElseThrow(ExceptionUtility::postCommentNotFoundException);
        if (!PostCommentUtility.isCommentAvailableForEditing(postComment, requestingUser)) {
            throw ExceptionUtility.forbiddenRightsException();
        }
        postComment.setText(postCommentEditModel.getText());
        postComment.setUpdatedAt(LocalDateTime.now());
        postCommentRepository.flush();
        return PostCommentMapper.toModel(postComment);
    }

    public List<TaskAnswerCommentModel> getTaskAnswerComments(UUID requestingUserId, UUID taskAnswerId) {
        return null;
    }

    public TaskAnswerCommentModel createTaskAnswerComment(
            UUID requestingUserId,
            UUID postId,
            TaskAnswerCommentCreateModel taskAnswerCommentCreateModel) {
        return null;
    }

    public TaskAnswerCommentModel editTaskAnswerComment(
            UUID requestingUserId,
            UUID postId,
            TaskAnswerCommentEditModel taskAnswerCommentEditModel) {
        return null;
    }

    private PostComment createPostComment(
            PostCommentCreateModel postCommentCreateModel,
            User requestingUser,
            Post post
    ) {
        return new PostComment()
                .setId(UUID.randomUUID())
                .setAuthor(requestingUser)
                .setPost(post)
                .setText(postCommentCreateModel.getText())
                .setCreatedAt(LocalDateTime.now());
    }

}
