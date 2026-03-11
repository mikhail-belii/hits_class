package com.example.hits.domain.service.post;

import com.example.hits.application.model.common.IdResponseModel;
import com.example.hits.application.model.file.FileModel;
import com.example.hits.application.model.post.PostCreateModel;
import com.example.hits.application.model.post.PostFullModel;
import com.example.hits.application.model.post.PostShortModel;
import com.example.hits.application.model.post.PostUpdateModel;
import com.example.hits.application.repository.*;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.application.util.PostUtility;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.file.File;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.mapper.PostMapper;
import com.example.hits.domain.service.taskanswer.TaskAnswerGeneralService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ExtensionMethod(PostMapper.class)
public class PostService {

    private final TaskAnswerGeneralService taskAnswerGeneralService;
    private final CourseRepository courseRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final TaskAnswerRepository taskAnswerRepository;

    @Transactional
    public IdResponseModel createPost(UUID courseId, UUID userId, PostCreateModel postCreateModel) {
        Course course = getCourseById(courseId);
        User user = findUserById(userId);

        if (!PostUtility.isAvailableForEditing(course, user)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        Post post = createPostFromModel(postCreateModel, user, course);
        post = postRepository.save(post);

        var files = buildPostFiles(postCreateModel.getFiles(), post, user, null);
        post.setFiles(files);
        if (files != null && !files.isEmpty()) {
            fileRepository.saveAll(files);
        }

        postRepository.save(post);

        if (post.getPostType() == PostType.TASK) {
            taskAnswerGeneralService.createTaskAnswerForEveryCourseMember(course, post);
        }

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
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .map(PostMapper::toModel)
                .toList();
    }

    public PostFullModel getPostInfo(UUID courseId, UUID postId, UUID userId) {
        Course course = getCourseById(courseId);
        User user = findUserById(userId);
        Post post = findPostById(postId);

        if (!PostUtility.isPostAvailableForReading(course, post, user)) {
            throw ExceptionUtility.badRequestException("You can't read this post");
        }

        var taskAnswer = taskAnswerRepository.findByUserIdAndPostId(userId, postId)
                .orElse(null);

        return PostMapper.toModel(post, taskAnswer);
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
        post.setFiles(buildPostFiles(postUpdateModel.getFiles(), post, user, post.getId()));
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

    private List<File> buildPostFiles(List<FileModel> fileModels,
                                            Post post,
                                            User user,
                                            UUID currentPostId) {
        var fileIds = extractFileIds(fileModels);
        if (fileIds.isEmpty()) {
            return post.getFiles();
        }

        var files = fileRepository.findAllById(fileIds);
        if (files.size() != fileIds.size()) {
            throw ExceptionUtility.badRequestException("One or more files not found");
        }

        var filesById = files.stream()
                .collect(Collectors.toMap(File::getId, Function.identity()));

        var newFiles = new ArrayList<File>();

        for (UUID fileId : fileIds) {
            var file = filesById.get(fileId);
            if (file == null) {
                throw ExceptionUtility.badRequestException("One or more files not found");
            }

            if (file.getUploader() == null || !file.getUploader().getId().equals(user.getId())) {
                throw ExceptionUtility.badRequestException("You can attach only your files");
            }

            var attachedToAnotherPost = file.getPost() != null
                    && (currentPostId == null || !file.getPost().getId().equals(currentPostId));
            if (attachedToAnotherPost || file.getTaskAnswer() != null) {
                throw ExceptionUtility.badRequestException("File is already attached");
            }

            newFiles.add(file);
        }

        if (post.getFiles() != null) {
            post.getFiles().forEach(file -> file.setPost(null));
        }

        for (File file : newFiles) {
            file.setPost(post);
            file.setTaskAnswer(null);
        }

        return newFiles;
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

