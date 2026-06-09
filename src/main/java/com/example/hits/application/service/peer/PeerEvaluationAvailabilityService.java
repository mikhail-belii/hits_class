package com.example.hits.application.service.peer;

import com.example.hits.application.mapper.SimpleUserMapper;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.application.util.PostUtility;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.domain.entity.post.TaskAnswerAppraisingType;
import com.example.hits.infrastructure.persistence.entity.PostEntity;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerEntity;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerStudentAppraiserEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import com.example.hits.infrastructure.persistence.repository.JpaTaskAnswerRepository;
import com.example.hits.infrastructure.persistence.repository.JpaTaskAnswerStudentAppraiserRepository;
import com.example.hits.infrastructure.persistence.repository.PostRepository;
import com.example.hits.infrastructure.persistence.repository.UserRepository;
import com.example.hits.presentation.dto.file.FileModel;
import com.example.hits.presentation.dto.taskanswer.AvailablePeerEvaluationModel;
import com.example.hits.presentation.dto.taskanswer.PeerEvaluationUnavailableReason;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PeerEvaluationAvailabilityService {

    private final JpaTaskAnswerRepository jpaTaskAnswerRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final JpaTaskAnswerStudentAppraiserRepository jpaAppraiserRepository;

    public List<AvailablePeerEvaluationModel> getAvailableWorksToAppraise(UUID postId, UUID userId) {
        var user = getUser(userId);
        var post = getPost(postId);

        validateRequest(post, user);

        var selectedAppraisers = jpaAppraiserRepository
                .findAllByStudentIdAndTaskAnswerEntity_PostEntityId(userId, postId);
        Set<UUID> selectedTaskAnswerIds = selectedAppraisers.stream()
                .map(TaskAnswerStudentAppraiserEntity::getTaskAnswerEntity)
                .filter(Objects::nonNull)
                .map(TaskAnswerEntity::getId)
                .collect(Collectors.toSet());

        var reciprocalStudentIds = findReciprocalStudentIds(postId, userId);
        var taskAnswers = jpaTaskAnswerRepository.findAllByPostEntityId(postId);
        Map<UUID, PeerEvaluationUnavailableReason> baseReasons = new HashMap<>();
        for (var taskAnswer : taskAnswers) {
            baseReasons.put(taskAnswer.getId(), getBaseUnavailableReason(
                    taskAnswer,
                    post,
                    userId,
                    selectedTaskAnswerIds,
                    selectedAppraisers.size()));
        }

        boolean hasAvailableNonReciprocalAnswer = taskAnswers.stream()
                .filter(taskAnswer -> baseReasons.get(taskAnswer.getId()) == null)
                .anyMatch(taskAnswer -> taskAnswer.getUserEntity() != null
                        && !reciprocalStudentIds.contains(taskAnswer.getUserEntity().getId()));

        return taskAnswers.stream()
                .map(taskAnswer -> toAvailablePeerEvaluationModel(
                        taskAnswer,
                        post,
                        baseReasons.get(taskAnswer.getId()),
                        reciprocalStudentIds,
                        hasAvailableNonReciprocalAnswer))
                .toList();
    }

    private void validateRequest(PostEntity post, UserEntity user) {
        if (post.getPostType() != PostType.TASK) {
            throw ExceptionUtility.badRequestException("Post is not a task type", "postType");
        }
        if (post.getTaskAnswerAppraisingType() != TaskAnswerAppraisingType.ANY) {
            throw ExceptionUtility.badRequestException("Available works are only supported for ANY appraising type",
                    "taskAnswerAppraisingType");
        }
        if (post.getCourseEntity() == null || !PostUtility.isUserInCourse(post.getCourseEntity(), user)) {
            throw ExceptionUtility.forbiddenRightsException();
        }
    }

    private Set<UUID> findReciprocalStudentIds(UUID postId, UUID userId) {
        return jpaAppraiserRepository.findByTaskAnswerEntity_PostEntityId(postId).stream()
                .filter(appraiser -> appraiser.getStudent() != null)
                .filter(appraiser -> appraiser.getTaskAnswerEntity() != null)
                .filter(appraiser -> appraiser.getTaskAnswerEntity().getUserEntity() != null)
                .filter(appraiser -> appraiser.getTaskAnswerEntity().getUserEntity().getId().equals(userId))
                .map(TaskAnswerStudentAppraiserEntity::getStudent)
                .map(UserEntity::getId)
                .collect(Collectors.toSet());
    }

    private PeerEvaluationUnavailableReason getBaseUnavailableReason(TaskAnswerEntity taskAnswer,
                                                                     PostEntity post,
                                                                     UUID userId,
                                                                     Set<UUID> selectedTaskAnswerIds,
                                                                     int selectedAppraisingCount) {
        var now = LocalDateTime.now();
        if (post.getDeadline() != null && post.getDeadline().isAfter(now)) {
            return PeerEvaluationUnavailableReason.TASK_DEADLINE_HAS_NOT_PASSED;
        }
        if (post.getAppraiserDeadline() != null && post.getAppraiserDeadline().isBefore(now)) {
            return PeerEvaluationUnavailableReason.APPRAISER_DEADLINE_HAS_PASSED;
        }
        if (taskAnswer.getSubmittedAt() == null) {
            return PeerEvaluationUnavailableReason.ANSWER_IS_NOT_SUBMITTED;
        }
        if (taskAnswer.getUserEntity() != null && taskAnswer.getUserEntity().getId().equals(userId)) {
            return PeerEvaluationUnavailableReason.OWN_ANSWER;
        }
        if (selectedTaskAnswerIds.contains(taskAnswer.getId())) {
            return PeerEvaluationUnavailableReason.ALREADY_SELECTED;
        }
        Integer appraisingLimit = post.getStudentAppraisingNumber();
        if (appraisingLimit != null && selectedAppraisingCount >= appraisingLimit) {
            return PeerEvaluationUnavailableReason.APPRAISING_LIMIT_REACHED;
        }
        return null;
    }

    private AvailablePeerEvaluationModel toAvailablePeerEvaluationModel(TaskAnswerEntity taskAnswer,
                                                                        PostEntity post,
                                                                        PeerEvaluationUnavailableReason reason,
                                                                        Set<UUID> reciprocalStudentIds,
                                                                        boolean hasAvailableNonReciprocalAnswer) {
        var finalReason = reason;
        var student = taskAnswer.getUserEntity();
        if (finalReason == null && student != null && reciprocalStudentIds.contains(student.getId())
                && hasAvailableNonReciprocalAnswer) {
            finalReason = PeerEvaluationUnavailableReason.RECIPROCAL_APPRAISING;
        }

        var hideStudent = post.getCanSeeAppraised() != null && !post.getCanSeeAppraised();
        return new AvailablePeerEvaluationModel()
                .setTaskAnswerId(taskAnswer.getId())
                .setStudent(!hideStudent && student != null ? SimpleUserMapper.toModel(student) : null)
                .setSubmittedAt(taskAnswer.getSubmittedAt())
                .setCanAppraise(finalReason == null)
                .setUnavailableReason(finalReason)
                .setFiles(taskAnswer.getFileEntities() != null
                        ? taskAnswer.getFileEntities().stream()
                            .map(f -> new FileModel(f.getId(), f.getOriginalName()))
                            .toList()
                        : List.of());
    }

    private UserEntity getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
    }

    private PostEntity getPost(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(ExceptionUtility::postNotFoundException);
    }
}
