package com.example.hits.infrastructure.persistence.repository;

import com.example.hits.application.mapper.*;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.domain.aggregate.CourseEvaluationAggregate;
import com.example.hits.domain.aggregate.ScoredPost;
import com.example.hits.domain.repository.JpaCourseRepository;
import com.example.hits.infrastructure.persistence.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.hits.domain.entity.post.PostType.TASK;

@Service
@RequiredArgsConstructor
public class CourseRepository {

    private final JpaTaskAnswerRepository taskAnswerRepository;
    private final UserCourseRepository userCourseRepository;

    public CourseEvaluationAggregate getCourseEvaluationAggregate(UUID userCourseId) {
        UserCourseEntity userCourseEntity = userCourseRepository.findById(userCourseId)
                .orElseThrow(ExceptionUtility::userCourseNotFoundException);
        CourseEntity courseEntity = userCourseEntity.getCourseEntity();

        List<TaskAnswerEntity> taskAnswers = taskAnswerRepository.findAllByUserEntityIdAndPostEntityCourseEntityId(
                userCourseEntity.getUserEntity().getId(),
                courseEntity.getId());
        Map<UUID, TaskAnswerEntity> taskAnswersMap = taskAnswers
                .stream()
                .collect(Collectors.toMap(t -> t.getPostEntity().getId(), Function.identity()));

        List<PostEntity> courseTasks = courseEntity.getPostEntities().stream()
                .filter(p -> TASK.equals(p.getPostType()))
                .toList();
        List<ScoredPost> scoredPosts = courseTasks.stream()
                .map(p -> PostMapper.toDomain(p, taskAnswersMap.get(p.getId())))
                .toList();

        return new CourseEvaluationAggregate(
                UserCourseMapper.toDomain(userCourseEntity),
                CourseMapper.toDomain(courseEntity),
                scoredPosts);
    }

    public void saveCourseEvaluationAggregate(CourseEvaluationAggregate courseEvaluationAggregate) {
        UserCourseEntity userCourseEntity = userCourseRepository.findById(courseEvaluationAggregate.getUserCourse().getId())
                .orElseThrow(ExceptionUtility::userCourseNotFoundException);

        userCourseEntity.setScore(courseEvaluationAggregate.getUserCourse().getScore());

        userCourseRepository.saveAndFlush(userCourseEntity);
    }

}
