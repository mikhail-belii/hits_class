package com.example.hits.domain.service.post;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.model.common.IdResponseModel;
import com.example.hits.application.model.post.PostCreateModel;
import com.example.hits.application.model.post.PostModel;
import com.example.hits.application.model.post.PostUpdateModel;
import com.example.hits.application.repository.CourseRepository;
import com.example.hits.application.repository.PostRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.application.util.PostUtility;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final CourseRepository courseRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public IdResponseModel createPost(UUID courseId, UUID userId, PostCreateModel postCreateModel) throws ExceptionWrapper {
        Course course = getCourseById(courseId);
        User user = findUserById(userId);

        if (!PostUtility.isAvailableForEditing(course, user)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        Post post = createPost(postCreateModel, user, course);

        postRepository.save(post);

        return new IdResponseModel(post.getId());
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

    private Post createPost(PostCreateModel postCreateModel, User author, Course course) {
        return new Post()
                .setId(UUID.randomUUID())
                .setText(postCreateModel.getText())
                .setCourse(course)
                .setAuthor(author)
                .setPostType(postCreateModel.getPostType())
                .setMaxScore(postCreateModel.getMaxScore())
                .setCreatedAt(LocalDateTime.now());
    }

    private Course getCourseById(UUID courseId) throws ExceptionWrapper {
        return courseRepository.findById(courseId)
                .orElseThrow(ExceptionUtility::courseNotFoundException);
    }

    private User findUserById(UUID userId) throws ExceptionWrapper {
        return userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
    }
}
