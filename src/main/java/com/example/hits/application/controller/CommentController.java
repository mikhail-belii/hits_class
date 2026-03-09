package com.example.hits.application.controller;

import com.example.hits.application.model.comment.postcomment.PostCommentCreateModel;
import com.example.hits.application.model.comment.postcomment.PostCommentEditModel;
import com.example.hits.application.model.comment.postcomment.PostCommentModel;
import com.example.hits.application.model.comment.taskanswercomment.TaskAnswerCommentCreateModel;
import com.example.hits.application.model.comment.taskanswercomment.TaskAnswerCommentEditModel;
import com.example.hits.application.model.comment.taskanswercomment.TaskAnswerCommentModel;
import com.example.hits.application.service.CommentService;
import com.example.hits.application.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/post/{postId}/comments")
    @Operation(summary = "Get post comments")
    public List<PostCommentModel> getPostComments(
            @RequestAttribute("userId") UUID requestingUserId,
            @PathVariable("postId") UUID postId
    ) {
        return commentService.getPostComments(requestingUserId, postId);
    }

    @PostMapping("/post/{postId}/comments")
    @Operation(summary = "Create post comment")
    public void createPostComment(
            @RequestAttribute("userId") UUID requestingUserId,
            @PathVariable("postId") UUID postId,
            @RequestBody PostCommentCreateModel postCommentCreateModel
    ) {
        commentService.createPostComment(requestingUserId, postId, postCommentCreateModel);
    }

    @PatchMapping("/post/comments/{postCommentId}")
    @Operation(summary = "Edit post comment")
    public void editPostComment(
            @RequestAttribute("userId") UUID requestingUserId,
            @PathVariable("postCommentId") UUID postCommentId,
            @RequestBody PostCommentEditModel postCommentEditModel
    ) {
        commentService.editPostComment(requestingUserId, postCommentId, postCommentEditModel);
    }

    @GetMapping("/task-answer/{taskAnswerId}/comments")
    @Operation(summary = "Get task answer comments")
    public List<TaskAnswerCommentModel> getTaskAnswerComments(
            @RequestAttribute("userId") UUID requestingUserId,
            @PathVariable("taskAnswerId") UUID taskAnswerId
    ) {
        return commentService.getTaskAnswerComments(requestingUserId, taskAnswerId);
    }

    @PostMapping("/task-answer/{taskAnswerId}/comments")
    @Operation(summary = "Create task answer comment")
    public void createTaskAnswerComment(
            @RequestAttribute("userId") UUID requestingUserId,
            @PathVariable("taskAnswerId") UUID postId,
            @RequestBody TaskAnswerCommentCreateModel taskAnswerCommentCreateModel
    ) {
        commentService.createTaskAnswerComment(requestingUserId, postId, taskAnswerCommentCreateModel);
    }

    @PatchMapping("/task-answer/comments/{taskAnswerCommentId}")
    @Operation(summary = "Edit task answer comment")
    public void editTaskAnswerComment(
            @RequestAttribute("userId") UUID requestingUserId,
            @PathVariable("taskAnswerCommentId") UUID taskAnswerCommentId,
            @RequestBody TaskAnswerCommentEditModel taskAnswerCommentEditModel
    ) {
        commentService.editTaskAnswerComment(requestingUserId, taskAnswerCommentId, taskAnswerCommentEditModel);
    }

}
