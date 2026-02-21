package com.example.hits.application.controller;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.model.common.IdResponseModel;
import com.example.hits.application.model.post.PostCreateModel;
import com.example.hits.application.model.post.PostModel;
import com.example.hits.application.model.post.PostUpdateModel;
import com.example.hits.domain.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public IdResponseModel createPost(@PathVariable UUID courseId,
                                      @RequestBody PostCreateModel postCreateModel,
                                      @RequestAttribute("userId") UUID userId) throws ExceptionWrapper {
        return postService.createPost(courseId, userId, postCreateModel);
    }

    @GetMapping
    public List<PostModel> getCoursePosts(@PathVariable UUID courseId, @RequestAttribute("userId") UUID userId) {
        return postService.getClassPosts(courseId, userId);
    }

    @GetMapping("/{postId}")
    public PostModel getPost(@PathVariable UUID courseId, @PathVariable UUID postId, @RequestAttribute("userId") UUID userId) {
        return postService.getPostInfo(courseId, postId, userId);
    }

    @PutMapping("/{postId}")
    public void updatePost(@PathVariable UUID courseId,
                           @PathVariable UUID postId,
                           @RequestBody PostUpdateModel postUpdateModel,
                           @RequestAttribute("userId") String userId) {
        postService.updatePost(courseId, postId, postUpdateModel);
    }

    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable UUID courseId, @PathVariable UUID postId, @RequestAttribute("userId") UUID userId) {
        postService.deletePost(courseId, postId, userId);
    }
}