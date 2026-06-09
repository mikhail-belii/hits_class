package com.example.hits.application.service.taskanswer;

import com.example.hits.application.service.peer.PeerEvaluationAvailabilityService;
import com.example.hits.infrastructure.persistence.entity.CourseEntity;
import com.example.hits.infrastructure.persistence.entity.CriteriaScoreEntity;
import com.example.hits.infrastructure.persistence.entity.MarkCriteriaEntity;
import com.example.hits.infrastructure.persistence.entity.PostEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import com.example.hits.presentation.dto.taskanswer.TaskAnswerCriteriaScoreModel;
import com.example.hits.presentation.dto.taskanswer.TaskAnswerFullModel;
import com.example.hits.presentation.dto.taskanswer.TaskAnswerModel;
import com.example.hits.infrastructure.persistence.repository.PostRepository;
import com.example.hits.infrastructure.persistence.repository.JpaTaskAnswerRepository;
import com.example.hits.infrastructure.persistence.repository.JpaTaskAnswerStudentAppraiserRepository;
import com.example.hits.infrastructure.persistence.repository.UserRepository;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.application.util.PostUtility;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.domain.entity.post.TaskMarkEvaluationType;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerEntity;
import com.example.hits.domain.entity.taskanswer.TaskAnswerStatus;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.infrastructure.persistence.entity.UserCourseEntity;
import com.example.hits.application.mapper.TaskAnswerMapper;
import com.example.hits.application.mapper.SimpleUserMapper;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerStudentAppraiserEntity;
import com.example.hits.presentation.dto.markcriteria.ScoredMarkCriteriaModel;
import com.example.hits.presentation.dto.file.FileModel;
import com.example.hits.presentation.dto.taskanswer.AvailablePeerEvaluationModel;
import com.example.hits.presentation.dto.taskanswer.PeerEvaluationModel;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskAnswerGeneralService {

    private final JpaTaskAnswerRepository jpaTaskAnswerRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final JpaTaskAnswerStudentAppraiserRepository jpaAppraiserRepository;
    private final PeerEvaluationAvailabilityService peerEvaluationAvailabilityService;

    Map<TaskAnswerStatus, Integer> priority = Map.of(
            TaskAnswerStatus.NEW, 1,
            TaskAnswerStatus.NOT_COMPLETED, 2,
            TaskAnswerStatus.COMPLETED, 3,
            TaskAnswerStatus.COMPETED_AFTER_DEADLINE, 4
    );

    @Transactional
    public void createTaskAnswerForEveryCourseMember(CourseEntity courseEntity, PostEntity postEntity) {
        if (courseEntity.getCourseUsers() == null || courseEntity.getCourseUsers().isEmpty()) {
            return;
        }

        var taskAnswers = new ArrayList<TaskAnswerEntity>(courseEntity.getCourseUsers().size());
        for (UserCourseEntity userCourseEntity : courseEntity.getCourseUsers()) {
            if (userCourseEntity.getUserRole() != UserCourseRole.STUDENT) {
                continue;
            }
            UserEntity userEntity = userCourseEntity.getUserEntity();

            taskAnswers.add(createTaskAnswerForDefiniteUser(postEntity, userEntity));
        }

        jpaTaskAnswerRepository.saveAll(taskAnswers);
    }

    public List<TaskAnswerModel> getAllUserTaskAnswers(UUID userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);

        return formAllUserTaskAnswers(userEntity).stream()
                .sorted(
                        Comparator
                                .comparing((TaskAnswerModel a) -> priority.get(a.getStatus()))
                                .thenComparing(TaskAnswerModel::getPostName))
                .toList();
    }

    public List<TaskAnswerFullModel> getAllPostTaskAnswers(UUID postId, UUID userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(ExceptionUtility::postNotFoundException);

        if (postEntity.getCourseEntity() == null || !PostUtility.isAvailableForEditing(postEntity.getCourseEntity(), userEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        if (postEntity.getPostType() != PostType.TASK) {
            throw ExceptionUtility.badRequestException("Post is not a task type");
        }

        return jpaTaskAnswerRepository.findAllByPostEntityId(postId).stream()
                .filter(taskAnswerModel -> taskAnswerModel.getSubmittedAt() != null)
                .map(TaskAnswerMapper::toFullModel)
                .sorted(Comparator
                        .comparing((TaskAnswerFullModel model) -> model.getUser().getFirstName(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(model -> model.getUser().getLastName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public TaskAnswerModel getUserPostTaskAnswer(UUID postId, UUID userId) {
        var taskAnswer = jpaTaskAnswerRepository.findByUserEntityIdAndPostEntityId(userId, postId)
                .orElseThrow(ExceptionUtility::taskAnswerNotFoundException);

        return TaskAnswerMapper.toModel(taskAnswer);
    }

    @Transactional
    public List<TaskAnswerCriteriaScoreModel> getCriteriaScoresForTaskAnswer(UUID taskAnswerId, UUID userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        TaskAnswerEntity taskAnswer = jpaTaskAnswerRepository.findById(taskAnswerId)
                .orElseThrow(ExceptionUtility::taskAnswerNotFoundException);

        if (!canViewTaskAnswerCriteriaScores(taskAnswer, userEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        PostEntity post = taskAnswer.getPostEntity();
        if (post.getPostType() != PostType.TASK) {
            throw ExceptionUtility.badRequestException("Criteria scores are only defined for task posts", "postType");
        }
        TaskMarkEvaluationType evaluationType = post.getTaskMarkEvaluationType();
        if (evaluationType == null || !evaluationType.isAnswerScoreIsCriteriaBased()) {
            throw ExceptionUtility.badRequestException(
                    "This task is not evaluated by criteria list", "taskMarkEvaluationType");
        }

        List<MarkCriteriaEntity> criteria = post.getMarkCriteriaEntityList();
        if (criteria == null || criteria.isEmpty()) {
            return List.of();
        }

        Map<UUID, CriteriaScoreEntity> scoreByCriteriaId = Optional.ofNullable(taskAnswer.getCriteriaScoreEntities())
                .orElseGet(List::of)
                .stream()
                .filter(Objects::nonNull)
                .filter(cs -> cs.getMarkCriteriaEntity() != null)
                .collect(Collectors.toMap(cs -> cs.getMarkCriteriaEntity().getId(), Function.identity(), (a, b) -> a));

        return criteria.stream()
                .map(mc -> {
                    CriteriaScoreEntity stored = scoreByCriteriaId.get(mc.getId());
                    Float score = stored != null ? stored.getScore() : null;
                    return new TaskAnswerCriteriaScoreModel()
                            .setMarkCriteriaId(mc.getId())
                            .setName(mc.getName())
                            .setDescription(mc.getDescription())
                            .setScore(score)
                            .setMinScore(mc.getMinScore())
                            .setMaxScore(mc.getMaxScore())
                            .setMultiplier(mc.getMultiplier())
                            .setEvaluationFunction(mc.getEvaluationFunction());
                })
                .toList();
    }

    private static boolean canViewTaskAnswerCriteriaScores(TaskAnswerEntity taskAnswer, UserEntity user) {
        if (taskAnswer.getUserEntity() != null && taskAnswer.getUserEntity().getId().equals(user.getId())) {
            return true;
        }
        PostEntity post = taskAnswer.getPostEntity();
        if (post == null || post.getCourseEntity() == null) {
            return false;
        }
        return PostUtility.isAvailableForEditing(post.getCourseEntity(), user);
    }

    public List<PeerEvaluationModel> getTasksToAppraise(UUID userId, UUID postId) {
        List<TaskAnswerStudentAppraiserEntity> appraiserEntities;
        if (postId != null) {
            appraiserEntities = jpaAppraiserRepository.findAllByStudentIdAndTaskAnswerEntity_PostEntityId(userId, postId);
        } else {
            appraiserEntities = jpaAppraiserRepository.findAllByStudentId(userId);
        }
        return appraiserEntities.stream()
                .map(this::toAppraiserModel)
                .toList();
    }

    public List<AvailablePeerEvaluationModel> getAvailableWorksToAppraise(UUID postId, UUID userId) {
        return peerEvaluationAvailabilityService.getAvailableWorksToAppraise(postId, userId);
    }

    private PeerEvaluationModel toAppraiserModel(TaskAnswerStudentAppraiserEntity entity) {
        var post = entity.getTaskAnswerEntity().getPostEntity();
        var evaluatedStudent = entity.getTaskAnswerEntity().getUserEntity();
        var appraiserStudent = entity.getStudent();

        Map<UUID, Float> scoreByCriteriaId = entity.getCriteriaScores() != null
                ? entity.getCriteriaScores().stream()
                    .filter(cs -> cs.getMarkCriteriaEntity() != null)
                    .collect(Collectors.toMap(cs -> cs.getMarkCriteriaEntity().getId(),
                            CriteriaScoreEntity::getScore, (a, b) -> a))
                : Map.of();

        List<ScoredMarkCriteriaModel> criteriaScoresList;
        if (post.getMarkCriteriaEntityList() != null && !post.getMarkCriteriaEntityList().isEmpty()) {
            criteriaScoresList = post.getMarkCriteriaEntityList().stream()
                    .map(mc -> new ScoredMarkCriteriaModel()
                            .setId(mc.getId())
                            .setScore(scoreByCriteriaId.get(mc.getId()))
                            .setName(mc.getName())
                            .setMinScore(mc.getMinScore())
                            .setMaxScore(mc.getMaxScore())
                            .setMultiplier(mc.getMultiplier())
                            .setEvaluationFunction(mc.getEvaluationFunction()))
                    .toList();
        } else {
            criteriaScoresList = List.of();
        }

        return new PeerEvaluationModel()
                .setId(entity.getId())
                .setStudent(evaluatedStudent != null ? SimpleUserMapper.toModel(evaluatedStudent) : null)
                .setAppraiser(appraiserStudent != null ? SimpleUserMapper.toModel(appraiserStudent) : null)
                .setScore(entity.getScore())
                .setSubmittedAt(entity.getSubmittedAt())
                .setTaskAnswerId(entity.getTaskAnswerEntity().getId())
                .setCriteriaScores(criteriaScoresList)
                .setFiles(entity.getTaskAnswerEntity().getFileEntities() != null
                        ? entity.getTaskAnswerEntity().getFileEntities().stream()
                            .map(f -> new FileModel(f.getId(), f.getOriginalName()))
                            .toList()
                        : List.of());
    }

    public List<PeerEvaluationModel> getAppraisersForTaskAnswer(UUID taskAnswerId, UUID userId) {
        var taskAnswer = jpaTaskAnswerRepository.findById(taskAnswerId)
                .orElseThrow(ExceptionUtility::taskAnswerNotFoundException);
        var userEntity = userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);

        if (taskAnswer.getUserEntity() == null || !taskAnswer.getUserEntity().getId().equals(userId)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        var appraisers = jpaAppraiserRepository.findAllByTaskAnswerEntityId(taskAnswerId);
        var post = taskAnswer.getPostEntity();
        var hideAppraiser = post.getCanSeeAppraiser() != null && !post.getCanSeeAppraiser();

        return appraisers.stream()
                .map(a -> {
                    var model = toAppraiserModel(a);
                    if (hideAppraiser) {
                        model.setAppraiser(null);
                    }
                    return model;
                })
                .toList();
    }

    public List<PeerEvaluationModel> getAllAppraisersForTaskAnswer(UUID taskAnswerId, UUID userId) {
        var taskAnswer = jpaTaskAnswerRepository.findById(taskAnswerId)
                .orElseThrow(ExceptionUtility::taskAnswerNotFoundException);
        var userEntity = userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);

        var post = taskAnswer.getPostEntity();
        if (post.getCourseEntity() == null || !PostUtility.isAvailableForEditing(post.getCourseEntity(), userEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        var appraisers = jpaAppraiserRepository.findAllByTaskAnswerEntityId(taskAnswerId);
        return appraisers.stream()
                .map(this::toAppraiserModel)
                .toList();
    }

    public PeerEvaluationModel getPeerEvaluationDetail(UUID evaluationId, UUID userId) {
        var appraiser = jpaAppraiserRepository.findById(evaluationId)
                .orElseThrow(ExceptionUtility::appraiserNotFoundException);

        if (!appraiser.getStudent().getId().equals(userId)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        var model = toAppraiserModel(appraiser);
        var post = appraiser.getTaskAnswerEntity().getPostEntity();
        var hideStudent = post.getCanSeeAppraised() != null && !post.getCanSeeAppraised();
        if (hideStudent) {
            model.setStudent(null);
        }
        return model;
    }

    public void createTaskAnswersForNewCourseUser(UserEntity userEntity, CourseEntity courseEntity) {
        List<PostEntity> coursePostEntities = postRepository.findAllByCourseEntityAndPostType(courseEntity, PostType.TASK);

        for (PostEntity postEntity : coursePostEntities) {
            if (jpaTaskAnswerRepository.findByUserEntityIdAndPostEntityId(userEntity.getId(), postEntity.getId()).isEmpty()) {
                createTaskAnswerForUser(postEntity, userEntity);
            }
        }
    }

    public void createTaskAnswerForUser(PostEntity postEntity, UserEntity userEntity) {
        TaskAnswerEntity newUserTaskAnswerEntity = createTaskAnswerForDefiniteUser(postEntity, userEntity);

        jpaTaskAnswerRepository.save(newUserTaskAnswerEntity);
    }

    private List<TaskAnswerModel> formAllUserTaskAnswers(UserEntity userEntity) {
        List<TaskAnswerModel> userTaskAnswers = new ArrayList<>();

        for (UserCourseEntity userCourseEntity : userEntity.getUserCoursEntities()) {
            if (userCourseEntity.getUserRole() == UserCourseRole.STUDENT) {
                userTaskAnswers.addAll(getAllUserCourseTaskAnswer(userEntity, userCourseEntity.getCourseEntity()));
            }
        }

        return userTaskAnswers;
    }

    private List<TaskAnswerModel> getAllUserCourseTaskAnswer(UserEntity userEntity, CourseEntity courseEntity) {
        return jpaTaskAnswerRepository.findAllByUserEntityIdAndPostEntityCourseEntityId(userEntity.getId(), courseEntity.getId()).stream()
                .map(TaskAnswerMapper::toModel)
                .toList();
    }

    private TaskAnswerEntity createTaskAnswerForDefiniteUser(PostEntity postEntity, UserEntity userEntity) {
        return new TaskAnswerEntity()
                .setPostEntity(postEntity)
                .setUserEntity(userEntity);
    }
}
