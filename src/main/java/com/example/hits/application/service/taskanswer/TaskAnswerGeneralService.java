package com.example.hits.application.service.taskanswer;

import com.example.hits.infrastructure.persistence.entity.CourseEntity;
import com.example.hits.infrastructure.persistence.entity.PostEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import com.example.hits.presentation.dto.taskanswer.TaskAnswerFullModel;
import com.example.hits.presentation.dto.taskanswer.TaskAnswerModel;
import com.example.hits.infrastructure.persistence.repository.PostRepository;
import com.example.hits.infrastructure.persistence.repository.TaskAnswerRepository;
import com.example.hits.infrastructure.persistence.repository.UserRepository;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.application.util.PostUtility;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerEntity;
import com.example.hits.domain.entity.taskanswer.TaskAnswerStatus;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.infrastructure.persistence.entity.UserCourseEntity;
import com.example.hits.application.mapper.TaskAnswerMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskAnswerGeneralService {

    private final TaskAnswerRepository taskAnswerRepository;
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

        taskAnswerRepository.saveAll(taskAnswers);
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

        return taskAnswerRepository.findAllByPostEntityId(postId).stream()
                .filter(taskAnswerModel -> taskAnswerModel.getSubmittedAt() != null)
                .map(TaskAnswerMapper::toFullModel)
                .sorted(Comparator
                        .comparing((TaskAnswerFullModel model) -> model.getUser().getFirstName(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(model -> model.getUser().getLastName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public TaskAnswerModel getUserPostTaskAnswer(UUID postId, UUID userId) {
        var taskAnswer = taskAnswerRepository.findByUserEntityIdAndPostEntityId(userId, postId)
                .orElseThrow(ExceptionUtility::taskAnswerNotFoundException);

        return TaskAnswerMapper.toModel(taskAnswer);
    }

    public void createTaskAnswersForNewCourseUser(UserEntity userEntity, CourseEntity courseEntity) {
        List<PostEntity> coursePostEntities = postRepository.findAllByCourseEntityAndPostType(courseEntity, PostType.TASK);

        for (PostEntity postEntity : coursePostEntities) {
            if (taskAnswerRepository.findByUserEntityIdAndPostEntityId(userEntity.getId(), postEntity.getId()).isEmpty()) {
                createTaskAnswerForUser(postEntity, userEntity);
            }
        }
    }

    public void createTaskAnswerForUser(PostEntity postEntity, UserEntity userEntity) {
        TaskAnswerEntity newUserTaskAnswerEntity = createTaskAnswerForDefiniteUser(postEntity, userEntity);

        taskAnswerRepository.save(newUserTaskAnswerEntity);
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
        return taskAnswerRepository.findAllByUserEntityIdAndPostEntityCourseEntityId(userEntity.getId(), courseEntity.getId()).stream()
                .map(TaskAnswerMapper::toModel)
                .toList();
    }

    private TaskAnswerEntity createTaskAnswerForDefiniteUser(PostEntity postEntity, UserEntity userEntity) {
        return new TaskAnswerEntity()
                .setPostEntity(postEntity)
                .setUserEntity(userEntity);
    }
}
