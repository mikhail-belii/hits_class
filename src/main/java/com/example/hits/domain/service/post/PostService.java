package com.example.hits.domain.service.post;

import com.example.hits.application.model.common.IdResponseModel;
import com.example.hits.application.model.file.FileModel;
import com.example.hits.application.model.post.PostCreateModel;
import com.example.hits.application.model.post.PostShortModel;
import com.example.hits.application.model.post.PostUpdateModel;
import com.example.hits.application.repository.*;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.application.util.PostUtility;
import com.example.hits.domain.entity.attachment.Attachment;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.file.File;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.mapper.PostMapper;
import com.example.hits.domain.service.taskanswer.TaskAnswerService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@ExtensionMethod(PostMapper.class)
public class PostService {

    private final TaskAnswerService taskAnswerService;
    private final CourseRepository courseRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final AttachmentRepository attachmentRepository;

    @Transactional
    public IdResponseModel createPost(UUID courseId, UUID userId, PostCreateModel postCreateModel) {
        Course course = getCourseById(courseId);
        User user = findUserById(userId);

        if (!PostUtility.isAvailableForEditing(course, user)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        Post post = createPostFromModel(postCreateModel, user, course);
        post.setAttachments(buildPostAttachments(postCreateModel.getFiles(), post, user, null));

        taskAnswerService.createTaskAnswerForEveryCourseMember(course, post);

        postRepository.save(post);

        return new IdResponseModel(post.getId());
    }

    public List<PostShortModel> getClassPosts(UUID courseId, UUID userId) {
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

    public PostShortModel getPostInfo(UUID courseId, UUID postId, UUID userId) {
        Course course = getCourseById(courseId);
        User user = findUserById(userId);
        Post post = findPostById(postId);

        if (!PostUtility.isPostAvailableForReading(course, post, user)) {
            throw ExceptionUtility.badRequestException("You can't read this post");
        }

        return post.toModel();
    }

    @Transactional
    public void updatePost(UUID courseId, UUID postId, UUID userId, PostUpdateModel postUpdateModel) {
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
        post.setAttachments(buildPostAttachments(postUpdateModel.getFiles(), post, user, post.getId()));
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    public void deletePost(UUID courseId, UUID postId, UUID userId) {
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

    private List<Attachment> buildPostAttachments(List<FileModel> fileModels,
                                                  Post post,
                                                  User user,
                                                  UUID currentPostId) {
        var fileIds = extractFileIds(fileModels);
        if (fileIds.isEmpty()) {
            return new ArrayList<>();
        }

        var files = fileRepository.findAllById(fileIds);
        if (files.size() != fileIds.size()) {
            throw ExceptionUtility.badRequestException("One or more files not found");
        }

        var filesById = files.stream()
                .collect(Collectors.toMap(File::getId, Function.identity()));

        var attachments = new ArrayList<Attachment>();
        for (UUID fileId : fileIds) {
            var file = filesById.get(fileId);
            if (file == null) {
                throw ExceptionUtility.badRequestException("One or more files not found");
            }

            if (file.getUploader() == null || !file.getUploader().getId().equals(user.getId())) {
                throw ExceptionUtility.badRequestException("You can attach only your files");
            }

//            var isAlreadyAttached = currentPostId == null
//                    ? attachmentRepository.existsByFile_Id(fileId)
//                    : attachmentRepository.existsByFile_IdAndPost_IdNot(fileId, currentPostId);
//            if (isAlreadyAttached) {
//                throw ExceptionUtility.badRequestException("File is already attached");
//            }

            attachments.add(new Attachment()
                    .setFile(file)
                    .setPost(post)
                    .setCreatedAt(LocalDateTime.now()));
        }

        return attachments;
    }

    private List<UUID> extractFileIds(List<FileModel> fileModels) {
        if (fileModels == null || fileModels.isEmpty()) {
            return List.of();
        }

        var uniqueIds = new LinkedHashSet<UUID>();
        for (FileModel fileModel : fileModels) {
            if (fileModel == null || fileModel.getId() == null) {
                throw ExceptionUtility.badRequestException("File id is required");
            }
            uniqueIds.add(fileModel.getId());
        }

        return new ArrayList<>(uniqueIds);
    }

    private Course getCourseById(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(ExceptionUtility::courseNotFoundException);
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
    }

    private Post findPostById(UUID userId) {
        return postRepository.findById(userId)
                .orElseThrow(ExceptionUtility::postNotFoundException);
    }
}
