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
import com.example.hits.domain.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ExtensionMethod(PostMapper.class)
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

        Post post = createPostFromModel(postCreateModel, user, course);

        postRepository.save(post);

        return new IdResponseModel(post.getId());
    }

    public List<PostModel> getClassPosts(UUID courseId, UUID userId) throws ExceptionWrapper {
        Course course = getCourseById(courseId);
        User user = findUserById(userId);

        if (!PostUtility.isUserInCourse(course, user)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        return postRepository.findAll().stream()
                .filter(post -> post.getCourse() != null && post.getCourse().equals(course))
                .map(PostMapper::toModel)
                .toList();
    }

    public PostModel getPostInfo(UUID courseId, UUID postId, UUID userId) throws ExceptionWrapper {
        Course course = getCourseById(courseId);
        User user = findUserById(userId);
        Post post = findPostById(postId);

        if (!PostUtility.isPostAvailableForReading(course, post, user)) {
            throw ExceptionUtility.badRequestException("You can't read this post");
        }

        return post.toModel();
    }

    public void updatePost(UUID courseId, UUID postId, UUID userId, PostUpdateModel postUpdateModel) throws ExceptionWrapper {
        Course course = getCourseById(courseId);
        User user = findUserById(userId);
        Post post = findPostById(postId);

        if (!PostUtility.isAvailableForEditing(course, user)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        if (post.getCourse() == null || !post.getCourse().equals(course)) {
            throw ExceptionUtility.badRequestException("You can't edit this post");
        }

        post.setText(postUpdateModel.getText());
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    public void deletePost(UUID courseId, UUID postId, UUID userId) throws ExceptionWrapper {
        Course course = getCourseById(courseId);
        User user = findUserById(userId);
        Post post = findPostById(postId);

        if (!PostUtility.isAvailableForEditing(course, user)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        if (post.getCourse() == null || !post.getCourse().equals(course)) {
            throw ExceptionUtility.badRequestException("You can't delete this post");
        }

        postRepository.delete(post);
    }

    private Post createPostFromModel(PostCreateModel postCreateModel, User author, Course course) {
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

    private Post findPostById(UUID userId) throws ExceptionWrapper {
        return postRepository.findById(userId)
                .orElseThrow(ExceptionUtility::postNotFoundException);
    }
}
