package com.example.hits.application.service.taskanswer;

import com.example.hits.application.mapper.UserCourseMapper;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.domain.aggregate.TaskEvaluationAggregate;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.domain.entity.post.TaskMarkEvaluationType;
import com.example.hits.domain.entity.usercourse.UserCourse;
import com.example.hits.infrastructure.persistence.entity.CriteriaScoreEntity;
import com.example.hits.infrastructure.persistence.entity.FileEntity;
import com.example.hits.infrastructure.persistence.entity.MarkCriteriaEntity;
import com.example.hits.infrastructure.persistence.entity.PostEntity;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerEntity;
import com.example.hits.infrastructure.persistence.entity.UserCourseEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import com.example.hits.infrastructure.persistence.repository.*;
import com.example.hits.presentation.dto.file.FileModel;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.presentation.request.taskanswer.CriteriaScoreRequest;
import com.example.hits.presentation.request.taskanswer.TaskRateRequestModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.hits.domain.entity.user.UserCourseRole.STUDENT;

@Service
@RequiredArgsConstructor
public class TaskAnswerUploadService {

    private final JpaTaskAnswerRepository jpaTaskAnswerRepository;
    private final TaskAnswerRepository taskAnswerRepository;
    private final UserRepository userRepository;
    private final UserCourseRepository userCourseRepository;
    private final FileRepository fileRepository;
    private final JpaCriteriaScoreRepository jpaCriteriaScoreRepository;
    private final CourseRepository courseRepository;

    public void evaluateTaskManually(UUID taskAnswerId, TaskRateRequestModel taskScore, UUID userId) {
        TaskEvaluationAggregate taskEvaluationAggregate = taskAnswerRepository.getTaskEvaluationAggregate(taskAnswerId);
        UserCourseEntity requestingUserCourse = userCourseRepository.findByTaskAnswerIdAndUserId(taskAnswerId, userId)
                .orElseThrow(ExceptionUtility::forbiddenRightsException);

        taskEvaluationAggregate.evaluateTaskManually(taskScore.getRate(), UserCourseMapper.toDomain(requestingUserCourse));

        taskAnswerRepository.saveTaskEvaluationAggregate(taskEvaluationAggregate);

        TaskAnswerEntity taskAnswerEntity = jpaTaskAnswerRepository.findById(taskAnswerId)
                .orElseThrow(ExceptionUtility::taskAnswerNotFoundException);
        recalculateCourseScoreForTaskAnswer(taskAnswerEntity);
    }

    public void recalculateTaskAnswerScoreFromStoredCriteria(UUID taskAnswerId) {
        TaskEvaluationAggregate aggregate = taskAnswerRepository.getTaskEvaluationAggregate(taskAnswerId);
        aggregate.evaluateTaskByCriteriaList();
        taskAnswerRepository.saveTaskEvaluationAggregate(aggregate);
    }

    public void recalculateScoresForAllPostTaskAnswers(UUID postId) {
        List<TaskAnswerEntity> taskAnswers = jpaTaskAnswerRepository.findAllByPostEntityId(postId);
        for (TaskAnswerEntity taskAnswer : taskAnswers) {
            recalculateTaskAnswerScoreFromStoredCriteria(taskAnswer.getId());
            recalculateCourseScoreForTaskAnswer(taskAnswer);
        }
    }

    private void recalculateCourseScoreForTaskAnswer(TaskAnswerEntity taskAnswer) {
        if (taskAnswer.getUserEntity() == null) {
            return;
        }
        UUID userId = taskAnswer.getUserEntity().getId();
        UUID courseId = taskAnswer.getPostEntity().getCourseEntity().getId();
        userCourseRepository.findAllByCourseEntityId(courseId).stream()
                .filter(uc -> uc.getUserEntity() != null && uc.getUserEntity().getId().equals(userId))
                .filter(uc -> UserCourseRole.STUDENT.equals(uc.getUserRole()))
                .findFirst()
                .ifPresent(uc -> {
                    var aggregate = courseRepository.getCourseEvaluationAggregate(uc.getId());
                    aggregate.evaluateCourseByTasks();
                    courseRepository.saveCourseEvaluationAggregate(aggregate);
                });
    }

    @Transactional
    public void putCriteriaScore(UUID taskAnswerId, CriteriaScoreRequest request, UUID userId) {
        UUID markCriteriaId = requireMarkCriteriaId(request);

        TaskAnswerEntity taskAnswerEntity = jpaTaskAnswerRepository.findById(taskAnswerId)
                .orElseThrow(ExceptionUtility::taskAnswerNotFoundException);
        PostEntity post = taskAnswerEntity.getPostEntity();

        assertTaskPostWithCriteriaBasedMarkEvaluation(post);

        MarkCriteriaEntity markCriteria = requireMarkCriteriaBelongingToPost(post, markCriteriaId);

        UserCourseEntity requestingUserCourse = userCourseRepository.findByUserEntityId(userId)
            .orElseThrow(ExceptionUtility::forbiddenRightsException);
        if (STUDENT.equals(requestingUserCourse.getUserRole())) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        persistSingleCriteriaScore(taskAnswerId, taskAnswerEntity, markCriteria, request.getScore(),
                post.getTaskMarkEvaluationType());
    }

    @Transactional
    public void putSelfAssessmentCriteriaScore(UUID taskAnswerId, CriteriaScoreRequest request, UUID userId) {
        UUID markCriteriaId = requireMarkCriteriaId(request);

        TaskAnswerEntity taskAnswerEntity = jpaTaskAnswerRepository.findById(taskAnswerId)
                .orElseThrow(ExceptionUtility::taskAnswerNotFoundException);
        PostEntity post = taskAnswerEntity.getPostEntity();

        if (post.getPostType() != PostType.TASK) {
            throw ExceptionUtility.badRequestException("Criteria scores are only allowed for task posts", "postType");
        }
        if (post.getTaskMarkEvaluationType() != TaskMarkEvaluationType.SELF_ASSESSMENT) {
            throw ExceptionUtility.badRequestException(
                    "Self-assessment criteria scores are only allowed when task mark evaluation type is SELF_ASSESSMENT",
                    "taskMarkEvaluationType");
        }

        MarkCriteriaEntity markCriteria = requireMarkCriteriaBelongingToPost(post, markCriteriaId);

        UserEntity owner = taskAnswerEntity.getUserEntity();
        if (owner == null || !owner.getId().equals(userId)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        persistSingleCriteriaScore(taskAnswerId, taskAnswerEntity, markCriteria, request.getScore(),
                post.getTaskMarkEvaluationType());
    }

    private static UUID requireMarkCriteriaId(CriteriaScoreRequest request) {
        UUID markCriteriaId = request != null ? request.getMarkCriteriaId() : null;
        if (markCriteriaId == null) {
            throw ExceptionUtility.badRequestException("Mark criteria id must not be null", "markCriteriaId");
        }
        return markCriteriaId;
    }

    private static void assertTaskPostWithCriteriaBasedMarkEvaluation(PostEntity post) {
        if (post.getPostType() != PostType.TASK) {
            throw ExceptionUtility.badRequestException("Criteria scores are only allowed for task posts", "postType");
        }
        TaskMarkEvaluationType taskMarkEvaluationType = post.getTaskMarkEvaluationType();
        if (taskMarkEvaluationType == null || !taskMarkEvaluationType.isAnswerScoreIsCriteriaBased()) {
            throw ExceptionUtility.badRequestException(
                    "Criteria scores are only allowed for criteria-based task mark evaluation types", "postType");
        }
    }

    private static MarkCriteriaEntity requireMarkCriteriaBelongingToPost(PostEntity post, UUID markCriteriaId) {
        List<MarkCriteriaEntity> criteriaList = post.getMarkCriteriaEntityList();
        if (criteriaList == null || criteriaList.isEmpty()) {
            throw ExceptionUtility.badRequestException("Task has no mark criteria");
        }

        Map<UUID, MarkCriteriaEntity> criteriaById = criteriaList.stream()
                .collect(Collectors.toMap(MarkCriteriaEntity::getId, Function.identity()));
        MarkCriteriaEntity markCriteria = criteriaById.get(markCriteriaId);
        if (markCriteria == null) {
            throw ExceptionUtility.badRequestException("Mark criteria does not belong to this task", "markCriteriaId");
        }
        return markCriteria;
    }

    private void persistSingleCriteriaScore(UUID taskAnswerId, TaskAnswerEntity taskAnswerEntity,
                                            MarkCriteriaEntity markCriteria, Float score,
                                            TaskMarkEvaluationType taskMarkEvaluationType) {
        validateCriteriaScore(markCriteria, score, taskMarkEvaluationType);

        UUID markCriteriaId = markCriteria.getId();
        CriteriaScoreEntity entity = jpaCriteriaScoreRepository
                .findByTaskAnswerEntity_IdAndMarkCriteriaEntity_Id(taskAnswerId, markCriteriaId)
                .orElseGet(() -> new CriteriaScoreEntity()
                        .setId(UUID.randomUUID())
                        .setMarkCriteriaEntity(markCriteria)
                        .setTaskAnswerEntity(taskAnswerEntity));
        entity.setScore(score);
        jpaCriteriaScoreRepository.save(entity);
        jpaTaskAnswerRepository.flush();

        recalculateTaskAnswerScoreFromStoredCriteria(taskAnswerId);
        recalculateCourseScoreForTaskAnswer(taskAnswerEntity);
    }

    private static void validateCriteriaScore(MarkCriteriaEntity markCriteria, Float score,
                                              TaskMarkEvaluationType taskMarkEvaluationType) {
        if (score == null) {
            throw ExceptionUtility.badRequestException("Criteria score must not be null", "score");
        }
        if (taskMarkEvaluationType == TaskMarkEvaluationType.PASS_FAIL
                && markCriteria.getMinScore() == null
                && markCriteria.getMaxScore() == null) {
            float s = score;
            if (s != 0f && s != 1f) {
                throw ExceptionUtility.badRequestException("Criteria score must be 0 or 1 for pass/fail mark criteria", "score");
            }
            return;
        }
        if (markCriteria.getMinScore() == null || markCriteria.getMaxScore() == null) {
            throw ExceptionUtility.badRequestException("Mark criteria is missing min or max score", "markCriteriaId");
        }
        if (score < markCriteria.getMinScore() || score > markCriteria.getMaxScore()) {
            throw ExceptionUtility.badRequestException("Criteria score is outside allowed range", "score");
        }
    }

    public void appendFiles(UUID taskAnswerId, List<FileModel> fileModels, UUID userId) {
        TaskAnswerEntity taskAnswerEntity = getTaskAnswer(taskAnswerId);
        UserEntity userEntity = getUser(userId);

        if (!taskAnswerEntity.getUserEntity().equals(userEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        if (taskAnswerEntity.getSubmittedAt() != null) {
            throw ExceptionUtility.badRequestException("Task already submitted");
        }

        taskAnswerEntity.setFileEntities(formFiles(taskAnswerEntity, fileModels.stream()
                .map(FileModel::getId)
                .toList()));

        jpaTaskAnswerRepository.save(taskAnswerEntity);
    }

    public void unpinFiles(UUID taskAnswerId, UUID fileId, UUID userId) {
        TaskAnswerEntity taskAnswerEntity = getTaskAnswer(taskAnswerId);
        UserEntity userEntity = getUser(userId);

        if (!taskAnswerEntity.getUserEntity().equals(userEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        if (taskAnswerEntity.getSubmittedAt() != null) {
            throw ExceptionUtility.badRequestException("Task already submitted");
        }

        boolean removed = taskAnswerEntity.getFileEntities().removeIf(file -> {
                    boolean shouldRemove = fileId.equals(file.getId());
                    if (shouldRemove) {
                        file.setTaskAnswerEntity(null);
                    }
                    return shouldRemove;
                }
        );

        if (!removed) {
            throw ExceptionUtility.badRequestException("File not found in attachments");
        }

        jpaTaskAnswerRepository.save(taskAnswerEntity);
    }

    public void submitTask(UUID taskAnswerId, UUID userId) {
        TaskAnswerEntity taskAnswerEntity = getTaskAnswer(taskAnswerId);
        UserEntity userEntity = getUser(userId);

        if (!taskAnswerEntity.getUserEntity().equals(userEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        taskAnswerEntity.setSubmittedAt(LocalDateTime.now());

        jpaTaskAnswerRepository.save(taskAnswerEntity);
    }

    public void unsubmitTask(UUID taskAnswerId, UUID userId) {
        TaskAnswerEntity taskAnswerEntity = getTaskAnswer(taskAnswerId);
        UserEntity userEntity = getUser(userId);

        if (!taskAnswerEntity.getUserEntity().equals(userEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        if (taskAnswerEntity.getScore() != 0) {
            throw ExceptionUtility.badRequestException("Task already evaluated");
        }

        taskAnswerEntity.setSubmittedAt(null);

        jpaTaskAnswerRepository.save(taskAnswerEntity);
    }

    private TaskAnswerEntity getTaskAnswer(UUID taskAnswerId) {
        return jpaTaskAnswerRepository.findById(taskAnswerId)
                .orElseThrow(ExceptionUtility::taskAnswerNotFoundException);
    }

    private UserEntity getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
    }

    private List<FileEntity> formFiles(TaskAnswerEntity taskAnswerEntity, List<UUID> fileIds) {
        var files = fileRepository.findAllById(fileIds);

        if (files.size() != fileIds.size()) {
            throw ExceptionUtility.badRequestException("One or more files not found");
        }

        var filesById = files.stream()
                .collect(Collectors.toMap(FileEntity::getId, Function.identity()));

        if (taskAnswerEntity.getFileEntities() != null) {
            taskAnswerEntity.getFileEntities().forEach(file -> file.setTaskAnswerEntity(null));
        }

        List<FileEntity> newFileEntities = new ArrayList<>();

        for (UUID fileId : fileIds) {
            var file = filesById.get(fileId);
            if (file == null) {
                throw ExceptionUtility.badRequestException("One or more files not found");
            }

            if (file.getPostEntity() != null || (file.getTaskAnswerEntity() != null && !file.getTaskAnswerEntity().equals(taskAnswerEntity))) {
                throw ExceptionUtility.badRequestException("File is already attached");
            }

            file.setPostEntity(null);
            file.setTaskAnswerEntity(taskAnswerEntity);
            newFileEntities.add(file);
        }

        return newFileEntities;
    }
}
