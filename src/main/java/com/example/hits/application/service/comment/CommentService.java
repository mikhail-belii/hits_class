package com.example.hits.application.service.comment;

import com.example.hits.infrastructure.persistence.entity.UserEntity;
import com.example.hits.infrastructure.persistence.entity.PostCommentEntity;
import com.example.hits.infrastructure.persistence.entity.PostEntity;
import com.example.hits.infrastructure.persistence.repository.*;
import com.example.hits.presentation.request.comment.PostCommentCreateModel;
import com.example.hits.presentation.request.comment.PostCommentEditModel;
import com.example.hits.presentation.dto.comment.postcomment.PostCommentModel;
import com.example.hits.presentation.request.comment.TaskAnswerCommentCreateModel;
import com.example.hits.presentation.request.comment.TaskAnswerCommentEditModel;
import com.example.hits.presentation.dto.comment.taskanswercomment.TaskAnswerCommentModel;
import com.example.hits.application.util.*;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerEntity;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerCommentEntity;
import com.example.hits.application.mapper.PostCommentMapper;
import com.example.hits.application.mapper.TaskAnswerCommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final UserRepository userRepository;

    private final TaskAnswerCommentRepository taskAnswerCommentRepository;

    private final PostCommentRepository postCommentRepository;

    private final PostRepository postRepository;

    private final TaskAnswerRepository taskAnswerRepository;

    public List<PostCommentModel> getPostComments(UUID requestingUserId, UUID postId) {
        com.example.hits.infrastructure.persistence.entity.UserEntity requestingUserEntity = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(ExceptionUtility::postNotFoundException);
        if (!PostUtility.isPostAvailableForReading(post.getCourseEntity(), post, requestingUserEntity)) {
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
        com.example.hits.infrastructure.persistence.entity.UserEntity requestingUserEntity = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(ExceptionUtility::postNotFoundException);
        if (!PostUtility.isPostAvailableForReading(post.getCourseEntity(), post, requestingUserEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }
        PostCommentEntity postComment = createPostComment(postCommentCreateModel, requestingUserEntity, post);
        postCommentRepository.saveAndFlush(postComment);
        return PostCommentMapper.toModel(postComment);
    }

    public PostCommentModel editPostComment(
            UUID requestingUserId,
            UUID postCommentId,
            PostCommentEditModel postCommentEditModel
    ) {
        com.example.hits.infrastructure.persistence.entity.UserEntity requestingUserEntity = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        PostCommentEntity postComment = postCommentRepository.findById(postCommentId)
                .orElseThrow(ExceptionUtility::postCommentNotFoundException);
        if (!PostCommentUtility.isCommentAvailableForEditing(postComment, requestingUserEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }
        postComment.setText(postCommentEditModel.getText());
        postComment.setUpdatedAt(LocalDateTime.now());
        postCommentRepository.flush();
        return PostCommentMapper.toModel(postComment);
    }

    public List<TaskAnswerCommentModel> getTaskAnswerComments(UUID requestingUserId, UUID taskAnswerId) {
        com.example.hits.infrastructure.persistence.entity.UserEntity requestingUserEntity = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        com.example.hits.infrastructure.persistence.entity.TaskAnswerEntity taskAnswerEntity = taskAnswerRepository.findById(taskAnswerId)
                .orElseThrow(ExceptionUtility::taskAnswerNotFoundException);
        if (!TaskAnswerCommentUtility.isTaskAnswerCommentsAvailableForUser(taskAnswerEntity, requestingUserEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }
        return taskAnswerEntity.getComments()
                .stream()
                .map(TaskAnswerCommentMapper::toModel)
                .toList();
    }

    public TaskAnswerCommentModel createTaskAnswerComment(
            UUID requestingUserId,
            UUID taskAnswerId,
            TaskAnswerCommentCreateModel taskAnswerCommentCreateModel
    ) {
        UserEntity requestingUserEntity = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        TaskAnswerEntity taskAnswerEntity = taskAnswerRepository.findById(taskAnswerId)
                .orElseThrow(ExceptionUtility::taskAnswerNotFoundException);
        if (!TaskAnswerCommentUtility.isTaskAnswerCommentsAvailableForUser(taskAnswerEntity, requestingUserEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }
        TaskAnswerCommentEntity taskAnswerCommentEntity = createTaskAnswerComment(taskAnswerCommentCreateModel, requestingUserEntity, taskAnswerEntity);
        taskAnswerCommentRepository.saveAndFlush(taskAnswerCommentEntity);
        return TaskAnswerCommentMapper.toModel(taskAnswerCommentEntity);
    }

    public TaskAnswerCommentModel editTaskAnswerComment(
            UUID requestingUserId,
            UUID taskAnswerCommentId,
            TaskAnswerCommentEditModel taskAnswerCommentEditModel
    ) {
        UserEntity requestingUserEntity = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        TaskAnswerCommentEntity taskAnswerCommentEntity = taskAnswerCommentRepository.findById(taskAnswerCommentId)
                .orElseThrow(ExceptionUtility::taskAnswerNotFoundException);
        if (!TaskAnswerCommentUtility.isCommentAvailableForEditing(taskAnswerCommentEntity, requestingUserEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }
        taskAnswerCommentEntity.setText(taskAnswerCommentEditModel.getText());
        taskAnswerCommentEntity.setUpdatedAt(LocalDateTime.now());
        taskAnswerCommentRepository.flush();
        return TaskAnswerCommentMapper.toModel(taskAnswerCommentEntity);
    }

    private PostCommentEntity createPostComment(
            PostCommentCreateModel postCommentCreateModel,
            com.example.hits.infrastructure.persistence.entity.UserEntity requestingUserEntity,
            PostEntity post
    ) {
        return new PostCommentEntity()
                .setId(UUID.randomUUID())
                .setAuthor(requestingUserEntity)
                .setPostEntity(post)
                .setText(postCommentCreateModel.getText())
                .setCreatedAt(LocalDateTime.now());
    }

    private TaskAnswerCommentEntity createTaskAnswerComment(
            TaskAnswerCommentCreateModel taskAnswerCommentCreateModel,
            UserEntity requestingUserEntity,
            TaskAnswerEntity taskAnswerEntity
    ) {
        return new TaskAnswerCommentEntity()
                .setId(UUID.randomUUID())
                .setAuthor(requestingUserEntity)
                .setTaskAnswerEntity(taskAnswerEntity)
                .setText(taskAnswerCommentCreateModel.getText())
                .setCreatedAt(LocalDateTime.now());
    }

}
