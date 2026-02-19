package com.example.hits.domain.service.post;

import com.example.hits.application.model.common.IdResponseModel;
import com.example.hits.application.model.post.PostModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    public IdResponseModel createPost() {
        return new IdResponseModel();
    }

    public List<PostModel> getClassPosts() {
        return new ArrayList<>();
    }

    public PostModel getPostInfo() {
        return new PostModel();
    }

    public void updatePost() {

    }

    public void deletePost() {

    }
}
