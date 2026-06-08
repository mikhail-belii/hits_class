package com.example.hits.infrastructure.persistence.repository;

import com.example.hits.application.mapper.CriteriaMapper;
import com.example.hits.application.mapper.PostMapper;
import com.example.hits.application.mapper.TaskAnswerMapper;
import com.example.hits.application.mapper.UserCourseMapper;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.application.util.PostUtility;
import com.example.hits.domain.aggregate.ScoredMarkCriteria;
import com.example.hits.domain.aggregate.TaskEvaluationAggregate;
import com.example.hits.infrastructure.persistence.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskAnswerRepository {

    private final JpaTaskAnswerRepository repository;
    private final UserRepository userRepository;

    public TaskEvaluationAggregate getTaskEvaluationAggregate(UUID taskAnswerId) {
        TaskAnswerEntity taskAnswerEntity = getTaskAnswer(taskAnswerId);
        UserEntity userEntity = taskAnswerEntity.getUserEntity();
        UserCourseEntity userCourseEntity = PostUtility.getUserCourse(taskAnswerEntity.getPostEntity().getCourseEntity(), userEntity)
                .orElseThrow(ExceptionUtility::forbiddenRightsException);

        Map<UUID, CriteriaScoreEntity> criteriaScoreMap = taskAnswerEntity.getCriteriaScoreEntities()
                .stream()
                .collect(Collectors.toMap(c -> c.getMarkCriteriaEntity().getId(), Function.identity()));

        List<ScoredMarkCriteria> scoredMarkCriteriaList = taskAnswerEntity.getPostEntity().getMarkCriteriaEntityList()
                .stream()
                .map(m -> CriteriaMapper.toDomain(m, criteriaScoreMap.get(m.getId())))
                .toList();

        return new TaskEvaluationAggregate(
                TaskAnswerMapper.toDomain(taskAnswerEntity),
                UserCourseMapper.toDomain(userCourseEntity),
                PostMapper.toDomain(taskAnswerEntity.getPostEntity()),
                scoredMarkCriteriaList);
    }

    public void saveTaskEvaluationAggregate(TaskEvaluationAggregate taskEvaluationAggregate) {
        TaskAnswerEntity taskAnswerEntity = getTaskAnswer(taskEvaluationAggregate.getTaskAnswer().getId());

        taskAnswerEntity.setTeacherScore(taskEvaluationAggregate.getTaskAnswer().getScore());
        repository.saveAndFlush(taskAnswerEntity);
    }

    public TaskAnswerEntity getTaskAnswer(UUID taskAnswerId) {
        return repository.findById(taskAnswerId)
                .orElseThrow(ExceptionUtility::taskAnswerNotFoundException);
    }

    private UserEntity getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
    }

}
