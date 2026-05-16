package com.example.hits.application.service.post;

import com.example.hits.domain.repository.JpaCourseRepository;
import com.example.hits.infrastructure.persistence.entity.FileEntity;
import com.example.hits.infrastructure.persistence.entity.PostEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import com.example.hits.infrastructure.persistence.repository.*;
import com.example.hits.presentation.dto.common.IdResponseModel;
import com.example.hits.presentation.dto.file.FileModel;
import com.example.hits.presentation.request.post.PostCreateModel;
import com.example.hits.presentation.dto.post.PostFullModel;
import com.example.hits.presentation.dto.post.PostShortModel;
import com.example.hits.presentation.request.post.PostUpdateModel;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.application.util.PostUtility;
import com.example.hits.infrastructure.persistence.entity.CourseEntity;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerCommentEntity;
import com.example.hits.application.mapper.PostMapper;
import com.example.hits.application.service.taskanswer.TaskAnswerGeneralService;
import com.example.hits.application.service.taskanswer.TaskAnswerUploadService;
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
    private final TaskAnswerUploadService taskAnswerUploadService;
    private final JpaCourseRepository jpaCourseRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final PostCommentRepository postCommentRepository;
    private final JpaTaskAnswerRepository jpaTaskAnswerRepository;
    private final TaskAnswerCommentRepository taskAnswerCommentRepository;

    @Transactional
    public IdResponseModel createPost(UUID courseId, UUID userId, PostCreateModel postCreateModel) {
        CourseEntity courseEntity = getCourseById(courseId);
        UserEntity userEntity = findUserById(userId);

        if (!PostUtility.isAvailableForEditing(courseEntity, userEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        validatePostCreation(postCreateModel);

        PostEntity postEntity = createPostFromModel(postCreateModel, userEntity, courseEntity);
        postEntity = postRepository.save(postEntity);

        var files = buildPostFiles(postCreateModel.getFiles(), postEntity, userEntity, null);
        postEntity.setFileEntities(files);
        if (files != null && !files.isEmpty()) {
            fileRepository.saveAll(files);
        }

        postRepository.save(postEntity);

        if (postEntity.getPostType() == PostType.TASK) {
            if (postEntity.getTaskMarkEvaluationType() == null) {
                throw ExceptionUtility.badRequestException("Task mark evaluation type must be not null when post is task");
            }
            postCreateModel.getTaskMarkEvaluationType().validatePostCreationByMarkEvaluationType(
                    postCreateModel,
                    courseEntity.getCourseMarkEvaluationType());

            taskAnswerGeneralService.createTaskAnswerForEveryCourseMember(courseEntity, postEntity);
        }

        return new IdResponseModel(postEntity.getId());
    }

    private void validatePostCreation(PostCreateModel postCreateModel) {
        if (postCreateModel.getPostType() != PostType.TASK) {
            return;
        }

        var deadline = postCreateModel.getDeadline();
        if (deadline != null && deadline.isBefore(LocalDateTime.now())) {
            throw ExceptionUtility.badRequestException("Deadline cannot be earlier than current moment");
        }
    }

    public List<PostShortModel> getClassPosts(UUID courseId, UUID userId) {
        CourseEntity courseEntity = getCourseById(courseId);
        UserEntity userEntity = findUserById(userId);

        if (!PostUtility.isUserInCourse(courseEntity, userEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        return postRepository.findAll().stream()
                .filter(post -> post.getCourseEntity() != null && post.getCourseEntity().equals(courseEntity))
                .sorted(Comparator.comparing(PostEntity::getCreatedAt).reversed())
                .map(PostMapper::toModel)
                .toList();
    }

    @Transactional(readOnly = true)
    public PostFullModel getPostInfo(UUID courseId, UUID postId, UUID userId) {
        CourseEntity courseEntity = getCourseById(courseId);
        UserEntity userEntity = findUserById(userId);
        PostEntity postEntity = findPostById(postId);

        if (!PostUtility.isPostAvailableForReading(courseEntity, postEntity, userEntity)) {
            throw ExceptionUtility.badRequestException("You can't read this post");
        }

        var taskAnswer = jpaTaskAnswerRepository.findByUserEntityIdAndPostEntityId(userId, postId)
                .orElse(null);

        return PostMapper.toModel(postEntity, taskAnswer);
    }

    @Transactional
    public void updatePost(UUID courseId, UUID postId, UUID userId, PostUpdateModel postUpdateModel) {
        CourseEntity courseEntity = getCourseById(courseId);
        UserEntity userEntity = findUserById(userId);
        PostEntity postEntity = findPostById(postId);

        if (!PostUtility.isAvailableForEditing(courseEntity, userEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        if (postEntity.getCourseEntity() == null || !postEntity.getCourseEntity().equals(courseEntity)) {
            throw ExceptionUtility.badRequestException("You can't edit this post");
        }
        if (postEntity.getPostType() == PostType.TASK) {
            postUpdateModel.getTaskMarkEvaluationType().validatePostCreationByMarkEvaluationType(
                    postUpdateModel,
                    courseEntity.getCourseMarkEvaluationType());
        }

        postEntity.setText(postUpdateModel.getText());
        postEntity.setFileEntities(buildPostFiles(postUpdateModel.getFiles(), postEntity, userEntity, postEntity.getId()));
        postEntity.setUpdatedAt(LocalDateTime.now());
        postEntity.setTaskMarkEvaluationType(postUpdateModel.getTaskMarkEvaluationType());
        postEntity.setMaxScore(postUpdateModel.getMaxScore());
        postEntity.setMinScore(postUpdateModel.getMinScore());
        postEntity.setEvaluationFunction(postUpdateModel.getEvaluationFunction());
        postEntity.setMultiplier(postUpdateModel.getMultiplier());
        postEntity.setPassThreshold(postUpdateModel.getPassThreshold());
        postRepository.save(postEntity);

        if (postEntity.getPostType() == PostType.TASK) {
            taskAnswerUploadService.recalculateScoresForAllPostTaskAnswers(postId);
        }
    }

    @Transactional
    public void deletePost(UUID courseId, UUID postId, UUID userId) {
        CourseEntity courseEntity = getCourseById(courseId);
        UserEntity userEntity = findUserById(userId);
        PostEntity postEntity = findPostById(postId);

        if (!PostUtility.isAvailableForEditing(courseEntity, userEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        if (postEntity.getCourseEntity() == null || !postEntity.getCourseEntity().equals(courseEntity)) {
            throw ExceptionUtility.badRequestException("You can't delete this post");
        }

        detachPostFiles(postEntity);
        deletePostComments(postEntity);
        deleteTaskAnswers(postId);
        postRepository.delete(postEntity);
    }

    private void detachPostFiles(PostEntity postEntity) {
        if (postEntity.getFileEntities() == null || postEntity.getFileEntities().isEmpty()) {
            return;
        }

        postEntity.getFileEntities().forEach(file -> file.setPostEntity(null));
        fileRepository.saveAll(postEntity.getFileEntities());
    }

    private void deletePostComments(PostEntity postEntity) {
        if (postEntity.getComments() == null || postEntity.getComments().isEmpty()) {
            return;
        }

        postCommentRepository.deleteAll(postEntity.getComments());
    }

    private void deleteTaskAnswers(UUID postId) {
        var taskAnswers = jpaTaskAnswerRepository.findAllByPostEntityId(postId);
        if (taskAnswers.isEmpty()) {
            return;
        }

        var attachedFiles = new ArrayList<FileEntity>();
        var comments = new ArrayList<TaskAnswerCommentEntity>();

        for (var taskAnswer : taskAnswers) {
            if (taskAnswer.getFileEntities() != null && !taskAnswer.getFileEntities().isEmpty()) {
                taskAnswer.getFileEntities().forEach(file -> file.setTaskAnswerEntity(null));
                attachedFiles.addAll(taskAnswer.getFileEntities());
            }

            if (taskAnswer.getComments() != null && !taskAnswer.getComments().isEmpty()) {
                comments.addAll(taskAnswer.getComments());
            }
        }

        if (!attachedFiles.isEmpty()) {
            fileRepository.saveAll(attachedFiles);
        }

        if (!comments.isEmpty()) {
            taskAnswerCommentRepository.deleteAll(comments);
        }

        jpaTaskAnswerRepository.deleteAll(taskAnswers);
    }

    private PostEntity createPostFromModel(PostCreateModel postCreateModel, UserEntity author, CourseEntity courseEntity) {
        LocalDateTime deadline = postCreateModel.getPostType() == PostType.TASK
                ? postCreateModel.getDeadline()
                : null;

        return new PostEntity()
                .setId(UUID.randomUUID())
                .setText(postCreateModel.getText())
                .setCourseEntity(courseEntity)
                .setAuthor(author)
                .setPostType(postCreateModel.getPostType())
                .setDeadline(deadline)
                .setMaxScore(postCreateModel.getMaxScore())
                .setCreatedAt(LocalDateTime.now())
                .setTaskMarkEvaluationType(postCreateModel.getTaskMarkEvaluationType())
                .setMaxScore(postCreateModel.getMaxScore())
                .setMinScore(postCreateModel.getMinScore())
                .setEvaluationFunction(postCreateModel.getEvaluationFunction())
                .setMultiplier(postCreateModel.getMultiplier())
                .setPassThreshold(postCreateModel.getPassThreshold());
    }

    private List<FileEntity> buildPostFiles(List<FileModel> fileModels,
                                            PostEntity postEntity,
                                            UserEntity userEntity,
                                            UUID currentPostId) {
        var fileIds = extractFileIds(fileModels);
        if (fileIds.isEmpty()) {
            return postEntity.getFileEntities();
        }

        var files = fileRepository.findAllById(fileIds);
        if (files.size() != fileIds.size()) {
            throw ExceptionUtility.badRequestException("One or more files not found");
        }

        var filesById = files.stream()
                .collect(Collectors.toMap(FileEntity::getId, Function.identity()));

        var newFiles = new ArrayList<FileEntity>();

        for (UUID fileId : fileIds) {
            var file = filesById.get(fileId);
            if (file == null) {
                throw ExceptionUtility.badRequestException("One or more files not found");
            }

            if (file.getUploader() == null || !file.getUploader().getId().equals(userEntity.getId())) {
                throw ExceptionUtility.badRequestException("You can attach only your files");
            }

            var attachedToAnotherPost = file.getPostEntity() != null
                    && (currentPostId == null || !file.getPostEntity().getId().equals(currentPostId));
            if (attachedToAnotherPost || file.getTaskAnswerEntity() != null) {
                throw ExceptionUtility.badRequestException("File is already attached");
            }

            newFiles.add(file);
        }

        if (postEntity.getFileEntities() != null) {
            postEntity.getFileEntities().forEach(file -> file.setPostEntity(null));
        }

        for (FileEntity fileEntity : newFiles) {
            fileEntity.setPostEntity(postEntity);
            fileEntity.setTaskAnswerEntity(null);
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

    private CourseEntity getCourseById(UUID courseId) {
        return jpaCourseRepository.findById(courseId)
                .orElseThrow(ExceptionUtility::courseNotFoundException);
    }

    private UserEntity findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
    }

    private PostEntity findPostById(UUID userId) {
        return postRepository.findById(userId)
                .orElseThrow(ExceptionUtility::postNotFoundException);
    }
}

