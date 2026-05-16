package com.example.hits.application.service.markcriteria;

import com.example.hits.application.mapper.CriteriaMapper;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.application.util.PostUtility;
import com.example.hits.application.service.taskanswer.TaskAnswerUploadService;
import com.example.hits.domain.entity.markCriteria.MarkCriteria;
import com.example.hits.domain.entity.markCriteria.MarkCriteriaDefinition;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.domain.repository.MarkCriteriaRepository;
import com.example.hits.infrastructure.persistence.entity.CourseEntity;
import com.example.hits.infrastructure.persistence.entity.PostEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import com.example.hits.domain.repository.JpaCourseRepository;
import com.example.hits.infrastructure.persistence.repository.PostRepository;
import com.example.hits.infrastructure.persistence.repository.UserRepository;
import com.example.hits.presentation.dto.common.IdResponseModel;
import com.example.hits.presentation.dto.markcriteria.MarkCriteriaModel;
import com.example.hits.presentation.request.markcriteria.MarkCriteriaWriteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MarkCriteriaService {

    private final JpaCourseRepository jpaCourseRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MarkCriteriaRepository markCriteriaRepository;
    private final TaskAnswerUploadService taskAnswerUploadService;

    @Transactional(readOnly = true)
    public List<MarkCriteriaModel> getMarkCriteria(UUID courseId, UUID postId, UUID userId) {
        CourseEntity course = getCourse(courseId);
        UserEntity user = findUser(userId);
        PostEntity post = findTaskPostInCourse(courseId, postId);
        if (!PostUtility.isPostAvailableForReading(course, post, user)) {
            throw ExceptionUtility.badRequestException("You can't read this post");
        }
        return markCriteriaRepository.findAllByPostId(postId).stream()
                .map(CriteriaMapper::toModel)
                .toList();
    }

    @Transactional
    public IdResponseModel createMarkCriteria(UUID courseId,
                                              UUID postId,
                                              UUID userId,
                                              MarkCriteriaWriteRequest request) {
        CourseEntity course = getCourse(courseId);
        UserEntity user = findUser(userId);
        ensureTeacher(course, user);

        PostEntity post = findTaskPostInCourse(courseId, postId);
        MarkCriteriaDefinition definition = toDefinition(request);
        MarkCriteria criteria = MarkCriteria.issue(UUID.randomUUID(), postId, definition, post.getTaskMarkEvaluationType());
        markCriteriaRepository.save(criteria);
        taskAnswerUploadService.recalculateScoresForAllPostTaskAnswers(postId);
        return new IdResponseModel(criteria.getId());
    }

    @Transactional
    public void updateMarkCriteria(UUID courseId,
                                   UUID postId,
                                   UUID markCriteriaId,
                                   UUID userId,
                                   MarkCriteriaWriteRequest request) {
        CourseEntity course = getCourse(courseId);
        UserEntity user = findUser(userId);
        ensureTeacher(course, user);

        PostEntity post = findTaskPostInCourse(courseId, postId);
        MarkCriteria criteria = markCriteriaRepository.findByIdAndPostId(markCriteriaId, postId)
                .orElseThrow(ExceptionUtility::markCriteriaNotFoundException);
        criteria.redefine(toDefinition(request), post.getTaskMarkEvaluationType());
        markCriteriaRepository.save(criteria);
        taskAnswerUploadService.recalculateScoresForAllPostTaskAnswers(postId);
    }

    @Transactional
    public void deleteMarkCriteria(UUID courseId, UUID postId, UUID markCriteriaId, UUID userId) {
        CourseEntity course = getCourse(courseId);
        UserEntity user = findUser(userId);
        ensureTeacher(course, user);

        findTaskPostInCourse(courseId, postId);

        if (!markCriteriaRepository.deleteWithScores(markCriteriaId, postId)) {
            throw ExceptionUtility.markCriteriaNotFoundException();
        }
        taskAnswerUploadService.recalculateScoresForAllPostTaskAnswers(postId);
    }

    private static MarkCriteriaDefinition toDefinition(MarkCriteriaWriteRequest request) {
        return new MarkCriteriaDefinition(
                request.getName(),
                request.getMinScore(),
                request.getMaxScore(),
                request.getMultiplier()
        );
    }

    private PostEntity findTaskPostInCourse(UUID courseId, UUID postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(ExceptionUtility::postNotFoundException);
        if (post.getCourseEntity() == null || !post.getCourseEntity().getId().equals(courseId)) {
            throw ExceptionUtility.badRequestException("Post does not belong to the requested course");
        }
        if (post.getPostType() != PostType.TASK) {
            throw ExceptionUtility.badRequestException("Mark criteria can only be managed for posts with type TASK");
        }
        return post;
    }

    private void ensureTeacher(CourseEntity course, UserEntity user) {
        if (!PostUtility.isAvailableForEditing(course, user)) {
            throw ExceptionUtility.forbiddenRightsException();
        }
    }

    private CourseEntity getCourse(UUID courseId) {
        return jpaCourseRepository.findById(courseId)
                .orElseThrow(ExceptionUtility::courseNotFoundException);
    }

    private UserEntity findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
    }
}
