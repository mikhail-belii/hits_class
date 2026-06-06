package com.example.hits.application.service.peer;

import com.example.hits.application.mapper.CriteriaMapper;
import com.example.hits.application.mapper.PostMapper;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.application.util.PostUtility;
import com.example.hits.domain.aggregate.TaskEvaluationAggregate;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.domain.entity.post.TaskAnswerAppraisingType;
import com.example.hits.domain.entity.post.TaskMarkEvaluationType;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.infrastructure.persistence.entity.CourseEntity;
import com.example.hits.infrastructure.persistence.entity.CriteriaScoreEntity;
import com.example.hits.infrastructure.persistence.entity.MarkCriteriaEntity;
import com.example.hits.infrastructure.persistence.entity.PostEntity;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerEntity;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerStudentAppraiserEntity;
import com.example.hits.infrastructure.persistence.entity.UserCourseEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import com.example.hits.infrastructure.persistence.repository.CourseRepository;
import com.example.hits.infrastructure.persistence.repository.JpaCriteriaScoreRepository;
import com.example.hits.infrastructure.persistence.repository.JpaTaskAnswerRepository;
import com.example.hits.infrastructure.persistence.repository.JpaTaskAnswerStudentAppraiserRepository;
import com.example.hits.infrastructure.persistence.repository.UserCourseRepository;
import com.example.hits.infrastructure.persistence.repository.UserRepository;
import com.example.hits.presentation.request.taskanswer.CriteriaScoreRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PeerEvaluationService {

    private final JpaTaskAnswerStudentAppraiserRepository jpaAppraiserRepository;
    private final JpaTaskAnswerRepository jpaTaskAnswerRepository;
    private final JpaCriteriaScoreRepository jpaCriteriaScoreRepository;
    private final UserCourseRepository userCourseRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Transactional
    public void generateChain(PostEntity postEntity, CourseEntity courseEntity) {
        if (postEntity.getTaskAnswerAppraisingType() != TaskAnswerAppraisingType.CHAIN) {
            return;
        }

        var students = courseEntity.getCourseUsers().stream()
                .filter(uc -> uc.getUserRole() == UserCourseRole.STUDENT)
                .map(UserCourseEntity::getUserEntity)
                .toList();

        if (students.size() < 2) {
            return;
        }

        var taskAnswers = jpaTaskAnswerRepository.findAllByPostEntityId(postEntity.getId());
        Map<UUID, TaskAnswerEntity> taskAnswerByUserId = new HashMap<>();
        for (var ta : taskAnswers) {
            if (ta.getUserEntity() != null) {
                taskAnswerByUserId.put(ta.getUserEntity().getId(), ta);
            }
        }

        var shuffledStudents = new ArrayList<>(students);
        Collections.shuffle(shuffledStudents);

        var appraiserEntities = new ArrayList<TaskAnswerStudentAppraiserEntity>();
        for (int i = 0; i < shuffledStudents.size(); i++) {
            var appraiserStudent = shuffledStudents.get(i);
            var targetStudent = shuffledStudents.get((i + 1) % shuffledStudents.size());
            var targetTaskAnswer = taskAnswerByUserId.get(targetStudent.getId());
            if (targetTaskAnswer == null) {
                continue;
            }

            appraiserEntities.add(new TaskAnswerStudentAppraiserEntity()
                    .setId(UUID.randomUUID())
                    .setStudent(appraiserStudent)
                    .setTaskAnswerEntity(targetTaskAnswer)
                    .setScore(0f));
        }

        jpaAppraiserRepository.saveAll(appraiserEntities);
    }

    @Transactional
    public void regenerateChainsForCourse(CourseEntity courseEntity) {
        var taskPosts = courseEntity.getPostEntities().stream()
                .filter(p -> PostType.TASK.equals(p.getPostType()))
                .filter(p -> TaskAnswerAppraisingType.CHAIN.equals(p.getTaskAnswerAppraisingType()))
                .filter(p -> p.getAppraiserDeadline() != null && p.getAppraiserDeadline().isAfter(LocalDateTime.now()))
                .toList();

        for (var post : taskPosts) {
            clearAppraiserRecordsForPost(post);
            generateChain(post, courseEntity);
        }
    }

    private void clearAppraiserRecordsForPost(PostEntity post) {
        var taskAnswers = jpaTaskAnswerRepository.findAllByPostEntityId(post.getId());
        for (var ta : taskAnswers) {
            if (ta.getStudentAppraiserEntities() != null) {
                for (var appraiser : ta.getStudentAppraiserEntities()) {
                    if (appraiser.getCriteriaScores() != null && !appraiser.getCriteriaScores().isEmpty()) {
                        jpaCriteriaScoreRepository.deleteAll(appraiser.getCriteriaScores());
                    }
                }
                jpaAppraiserRepository.deleteAll(ta.getStudentAppraiserEntities());
            }
        }
    }

    @Transactional
    public void submitAppraiserScore(UUID appraiserId, List<CriteriaScoreRequest> criteriaScores, UUID userId) {
        var appraiser = jpaAppraiserRepository.findById(appraiserId)
                .orElseThrow(ExceptionUtility::appraiserNotFoundException);

        if (appraiser.getSubmittedAt() != null) {
            throw ExceptionUtility.badRequestException("Appraiser has already submitted their evaluation");
        }

        if (!appraiser.getStudent().getId().equals(userId)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        var post = appraiser.getTaskAnswerEntity().getPostEntity();
        if (post.getAppraiserDeadline() != null && post.getAppraiserDeadline().isBefore(LocalDateTime.now())) {
            throw ExceptionUtility.badRequestException("Appraiser deadline has passed");
        }

        for (var request : criteriaScores) {
            var markCriteria = requireMarkCriteriaBelongingToPost(post, request.getMarkCriteriaId());
            validateCriteriaScore(markCriteria, request.getScore(), post.getTaskMarkEvaluationType());

            var entity = jpaCriteriaScoreRepository
                    .findByTaskAnswerStudentAppraiserEntity_IdAndMarkCriteriaEntity_Id(appraiserId, request.getMarkCriteriaId())
                    .orElseGet(() -> new CriteriaScoreEntity()
                            .setId(UUID.randomUUID())
                            .setMarkCriteriaEntity(markCriteria)
                            .setTaskAnswerStudentAppraiserEntity(appraiser));
            entity.setScore(request.getScore());
            jpaCriteriaScoreRepository.save(entity);
        }

        recalculateAppraiserScore(appraiser);
        jpaAppraiserRepository.save(appraiser);
    }

    @Transactional
    public void finalizeAppraiserEvaluation(UUID appraiserId, UUID userId) {
        var appraiser = jpaAppraiserRepository.findById(appraiserId)
                .orElseThrow(ExceptionUtility::appraiserNotFoundException);

        if (appraiser.getSubmittedAt() != null) {
            throw ExceptionUtility.badRequestException("Appraiser has already submitted their evaluation");
        }

        if (!appraiser.getStudent().getId().equals(userId)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        var post = appraiser.getTaskAnswerEntity().getPostEntity();
        if (post.getAppraiserDeadline() != null && post.getAppraiserDeadline().isBefore(LocalDateTime.now())) {
            throw ExceptionUtility.badRequestException("Appraiser deadline has passed");
        }

        recalculateAppraiserScore(appraiser);
        appraiser.setSubmittedAt(LocalDateTime.now());
        jpaAppraiserRepository.save(appraiser);

        recalculateTaskAnswerScoreFromAppraisers(appraiser.getTaskAnswerEntity());
        recalculateCourseScoreForTaskAnswer(appraiser.getTaskAnswerEntity());
    }

    @Transactional
    public void overrideAppraiserCriteria(UUID appraiserId, List<CriteriaScoreRequest> criteriaScores, UUID userId) {
        var appraiser = jpaAppraiserRepository.findById(appraiserId)
                .orElseThrow(ExceptionUtility::appraiserNotFoundException);
        var requestingUser = getUser(userId);

        var post = appraiser.getTaskAnswerEntity().getPostEntity();
        assertCanEditCourse(post.getCourseEntity(), requestingUser);

        for (var request : criteriaScores) {
            var markCriteria = requireMarkCriteriaBelongingToPost(post, request.getMarkCriteriaId());
            validateCriteriaScore(markCriteria, request.getScore(), post.getTaskMarkEvaluationType());

            var entity = jpaCriteriaScoreRepository
                    .findByTaskAnswerStudentAppraiserEntity_IdAndMarkCriteriaEntity_Id(appraiserId, request.getMarkCriteriaId())
                    .orElseGet(() -> new CriteriaScoreEntity()
                            .setId(UUID.randomUUID())
                            .setMarkCriteriaEntity(markCriteria)
                            .setTaskAnswerStudentAppraiserEntity(appraiser));
            entity.setScore(request.getScore());
            jpaCriteriaScoreRepository.save(entity);
        }

        recalculateAppraiserScore(appraiser);
        if (appraiser.getSubmittedAt() == null) {
            appraiser.setSubmittedAt(LocalDateTime.now());
        }
        jpaAppraiserRepository.save(appraiser);

        recalculateTaskAnswerScoreFromAppraisers(appraiser.getTaskAnswerEntity());
        recalculateCourseScoreForTaskAnswer(appraiser.getTaskAnswerEntity());
    }

    @Transactional
    public void overrideAppraiserScore(UUID appraiserId, Float score, UUID userId) {
        var appraiser = jpaAppraiserRepository.findById(appraiserId)
                .orElseThrow(ExceptionUtility::appraiserNotFoundException);
        var requestingUser = getUser(userId);

        var post = appraiser.getTaskAnswerEntity().getPostEntity();
        assertCanEditCourse(post.getCourseEntity(), requestingUser);

        if (score < 0 || (post.getMaxScore() != null && score > post.getMaxScore())) {
            throw ExceptionUtility.badRequestException("Score is outside allowed range");
        }

        appraiser.setScore(score);
        if (appraiser.getSubmittedAt() == null) {
            appraiser.setSubmittedAt(LocalDateTime.now());
        }
        jpaAppraiserRepository.save(appraiser);

        recalculateTaskAnswerScoreFromAppraisers(appraiser.getTaskAnswerEntity());
        recalculateCourseScoreForTaskAnswer(appraiser.getTaskAnswerEntity());
    }

    private void recalculateAppraiserScore(TaskAnswerStudentAppraiserEntity appraiser) {
        var criteriaScores = appraiser.getCriteriaScores();
        if (criteriaScores == null || criteriaScores.isEmpty()) {
            return;
        }

        var post = appraiser.getTaskAnswerEntity().getPostEntity();
        var domainPost = PostMapper.toDomain(post);
        var domainTaskAnswer = new TaskAnswer()
                .setId(appraiser.getTaskAnswerEntity().getId())
                .setScore(appraiser.getScore());

        var scoredMarkCriteria = criteriaScores.stream()
                .filter(cs -> cs.getMarkCriteriaEntity() != null)
                .map(cs -> CriteriaMapper.toDomain(cs.getMarkCriteriaEntity(), cs))
                .toList();

        if (scoredMarkCriteria.isEmpty()) {
            return;
        }

        var aggregate = new TaskEvaluationAggregate(
                domainTaskAnswer,
                null,
                domainPost,
                scoredMarkCriteria);

        aggregate.evaluateTaskByCriteriaList();
        appraiser.setScore(domainTaskAnswer.getScore());
    }

    private void recalculateTaskAnswerScoreFromAppraisers(TaskAnswerEntity taskAnswer) {
        var appraisers = taskAnswer.getStudentAppraiserEntities();
        if (appraisers == null || appraisers.isEmpty()) {
            return;
        }

        var submittedAppraisers = appraisers.stream()
                .filter(a -> a.getSubmittedAt() != null)
                .toList();

        if (submittedAppraisers.isEmpty()) {
            return;
        }

        float avgScore = (float) submittedAppraisers.stream()
                .mapToDouble(a -> a.getScore() != null ? a.getScore() : 0f)
                .average()
                .orElse(0f);

        taskAnswer.setScore(avgScore);
        jpaTaskAnswerRepository.save(taskAnswer);
    }

    private void recalculateCourseScoreForTaskAnswer(TaskAnswerEntity taskAnswer) {
        if (taskAnswer.getUserEntity() == null || taskAnswer.getPostEntity() == null
                || taskAnswer.getPostEntity().getCourseEntity() == null) {
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

    private MarkCriteriaEntity requireMarkCriteriaBelongingToPost(PostEntity post, UUID markCriteriaId) {
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

    private void validateCriteriaScore(MarkCriteriaEntity markCriteria, Float score,
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

    private UserEntity getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
    }

    private void assertCanEditCourse(CourseEntity course, UserEntity user) {
        if (course == null || !PostUtility.isAvailableForEditing(course, user)) {
            throw ExceptionUtility.forbiddenRightsException();
        }
    }
}
