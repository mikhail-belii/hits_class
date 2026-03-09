package com.example.hits.application.service;

import com.example.hits.application.model.comment.postcomment.PostCommentCreateModel;
import com.example.hits.application.model.comment.postcomment.PostCommentEditModel;
import com.example.hits.application.model.comment.postcomment.PostCommentModel;
import com.example.hits.application.model.comment.taskanswercomment.TaskAnswerCommentCreateModel;
import com.example.hits.application.model.comment.taskanswercomment.TaskAnswerCommentEditModel;
import com.example.hits.application.model.comment.taskanswercomment.TaskAnswerCommentModel;

import java.util.List;
import java.util.UUID;

public interface CommentService {

    List<PostCommentModel> getPostComments(UUID requestingUserId, UUID postId);

    PostCommentModel createPostComment(
            UUID requestingUserId,
            UUID postId,
            PostCommentCreateModel postCommentCreateModel);

    PostCommentModel editPostComment(
            UUID requestingUserId,
            UUID postId,
            PostCommentEditModel postCommentEditModel);

    List<TaskAnswerCommentModel> getTaskAnswerComments(UUID requestingUserId, UUID taskAnswerId);

    TaskAnswerCommentModel createTaskAnswerComment(
            UUID requestingUserId,
            UUID postId,
            TaskAnswerCommentCreateModel taskAnswerCommentCreateModel);

    TaskAnswerCommentModel editTaskAnswerComment(
            UUID requestingUserId,
            UUID postId,
            TaskAnswerCommentEditModel taskAnswerCommentEditModel);
}
