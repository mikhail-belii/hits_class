package com.example.hits.infrastructure.persistence.repository;

import com.example.hits.application.mapper.PostMapper;
import com.example.hits.application.mapper.TaskAnswerMapper;
import com.example.hits.application.mapper.UserCourseMapper;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.application.util.PostUtility;
import com.example.hits.domain.aggregate.TaskEvaluationAggregate;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerEntity;
import com.example.hits.infrastructure.persistence.entity.UserCourseEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskAnswerRepositoryImpl {

    private final TaskAnswerRepository repository;
    private final UserRepository userRepository;

    public TaskEvaluationAggregate getTaskEvaluationAggregate(UUID taskAnswerId, UUID userId) {
        TaskAnswerEntity taskAnswerEntity = getTaskAnswer(taskAnswerId);
        UserEntity userEntity = getUser(userId);
        UserCourseEntity userCourseEntity = PostUtility.getUserCourse(taskAnswerEntity.getPostEntity().getCourseEntity(), userEntity)
                .orElseThrow(ExceptionUtility::forbiddenRightsException);

        return new TaskEvaluationAggregate(
                TaskAnswerMapper.toDomain(taskAnswerEntity),
                UserCourseMapper.toDomain(userCourseEntity),
                PostMapper.toDomain(taskAnswerEntity.getPostEntity()));
    }

    public void saveTaskEvaluationAggregate(TaskEvaluationAggregate taskEvaluationAggregate) {
        TaskAnswerEntity taskAnswerEntity = getTaskAnswer(taskEvaluationAggregate.getTaskAnswer().getId());

        taskAnswerEntity.setScore(taskEvaluationAggregate.getTaskAnswer().getScore());
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
