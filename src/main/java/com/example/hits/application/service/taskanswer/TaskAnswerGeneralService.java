package com.example.hits.application.service.taskanswer;

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
                            .setScore(score);
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
