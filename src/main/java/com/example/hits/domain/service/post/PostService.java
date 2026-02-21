package com.example.hits.domain.service.post;

import com.example.hits.application.model.common.IdResponseModel;
import com.example.hits.application.model.post.PostCreateModel;
import com.example.hits.application.model.post.PostModel;
import com.example.hits.application.model.post.PostUpdateModel;
import com.example.hits.application.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public IdResponseModel createPost(UUID courseId, UUID userId, PostCreateModel postCreateModel) {
        return new IdResponseModel();
    }

    public List<PostModel> getClassPosts(UUID courseId, UUID userId) {
        return new ArrayList<>();
    }

    public PostModel getPostInfo(UUID courseId, UUID postId, UUID userId) {
        return new PostModel();
    }

    public void updatePost(UUID courseId, UUID userId, PostUpdateModel postUpdateModel) {

    }

    public void deletePost(UUID courseId, UUID postId, UUID userId) {

    }
}
