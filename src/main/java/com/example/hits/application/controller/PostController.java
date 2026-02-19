package com.example.hits.application.controller;

import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("create")
    public void createPost(@RequestBody Post post) {

    }
}
