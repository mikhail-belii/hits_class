package com.example.hits.application.service.course;

import com.example.hits.domain.aggregate.CourseEvaluationAggregate;
import com.example.hits.domain.aggregate.TaskEvaluationAggregate;
import com.example.hits.infrastructure.persistence.entity.CourseEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import com.example.hits.domain.repository.JpaCourseRepository;
import com.example.hits.infrastructure.persistence.repository.CourseRepository;
import com.example.hits.infrastructure.persistence.repository.UserCourseRepository;
import com.example.hits.infrastructure.persistence.repository.UserRepository;
import com.example.hits.application.util.CourseUtility;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.infrastructure.persistence.entity.UserCourseEntity;
import com.example.hits.application.mapper.CourseMapper;
import com.example.hits.application.mapper.UserCourseMapper;
import com.example.hits.application.service.taskanswer.TaskAnswerGeneralService;
import com.example.hits.presentation.dto.course.*;
import com.example.hits.presentation.request.course.CourseCreateModel;
import com.example.hits.presentation.request.course.CourseEditModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final UserRepository userRepository;
    private final JpaCourseRepository jpaCourseRepository;
    private final UserCourseRepository userCourseRepository;
    private final CourseCodeGenerator courseCodeGenerator;
    private final TaskAnswerGeneralService taskAnswerGeneralService;
    private final CourseRepository courseRepository;

    @Transactional
    public CourseModel createCourse(UUID requestingUserId, CourseCreateModel courseCreateModel) {
        UserEntity requestingUserEntity = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);

        courseCreateModel.getCourseMarkEvaluationType().validateCourseCreatingByMarkEvaluationType(courseCreateModel);

        CourseEntity courseEntity = createCourseFromModel(courseCreateModel);
        UserCourseEntity userCourseEntity = createUserCourseOnCourseCreation(courseEntity, requestingUserEntity);

        jpaCourseRepository.save(courseEntity);
        userCourseRepository.save(userCourseEntity);

        jpaCourseRepository.flush();

        return CourseMapper.toModel(courseEntity, userCourseEntity);
    }

    public void editCourse(UUID requestingUserId, UUID courseId, CourseEditModel courseEditModel) {
        UserEntity requestingUserEntity = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        CourseEntity editingCourseEntity = jpaCourseRepository.findById(courseId)
                .orElseThrow(ExceptionUtility::courseNotFoundException);

        if (!CourseUtility.isCourseAvailableForEditing(editingCourseEntity, requestingUserEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        courseEditModel.getCourseMarkEvaluationType().validateCourseCreatingByMarkEvaluationType(courseEditModel);

        editingCourseEntity
            .setName(courseEditModel.getName())
            .setDescription(courseEditModel.getDescription())
            .setCourseMarkEvaluationType(courseEditModel.getCourseMarkEvaluationType())
            .setPassThreshold(courseEditModel.getPassThreshold());

        jpaCourseRepository.saveAndFlush(editingCourseEntity);
    }

    public void evaluateUserCourseScore(UUID userCourseId) {
        CourseEvaluationAggregate courseEvaluationAggregate = courseRepository.getCourseEvaluationAggregate(userCourseId);

        courseEvaluationAggregate.evaluateCourseByTasks();

        courseRepository.saveCourseEvaluationAggregate(courseEvaluationAggregate);
    }

    public void archiveCourse(UUID requestingUserId, boolean isArchived, UUID courseId) {
        UserEntity requestingUserEntity = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        CourseEntity editingCourseEntity = jpaCourseRepository.findById(courseId)
                .orElseThrow(ExceptionUtility::courseNotFoundException);

        if (!CourseUtility.isCourseAvailableForEditing(editingCourseEntity, requestingUserEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        editingCourseEntity.setIsArchived(isArchived);

        jpaCourseRepository.saveAndFlush(editingCourseEntity);
    }

    public List<UserCourseModel> getCourseUsers(UUID requestingUserId, UUID courseId) {
        UserEntity requestingUserEntity = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        CourseEntity courseEntity = jpaCourseRepository.findById(courseId)
                .orElseThrow(ExceptionUtility::courseNotFoundException);

        if (CourseUtility.getUserCourse(courseEntity, requestingUserEntity).isEmpty()) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        return courseEntity.getCourseUsers()
                .stream()
                .map(UserCourseMapper::toModel)
                .toList();
    }

    public CourseModel getConcreteCourse(UUID requestingUserId, UUID courseId) {
        UserEntity requestingUserEntity = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        CourseEntity courseEntity = jpaCourseRepository.findById(courseId)
                .orElseThrow(ExceptionUtility::courseNotFoundException);

        UserCourseEntity userCourseEntity = CourseUtility.getUserCourse(courseEntity, requestingUserEntity)
                .orElseThrow(ExceptionUtility::forbiddenRightsException);


        return CourseMapper.toModel(courseEntity, userCourseEntity);
    }

    public List<CourseShortModel> getUserCourses(UUID requestingUserId, boolean isArchived) {
        UserEntity requestingUserEntity = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);

        return requestingUserEntity.getUserCoursEntities()
                .stream()
                .filter(c -> c.getCourseEntity().getIsArchived() == isArchived)
                .map(c -> CourseMapper.toShortModel(c.getCourseEntity(), c.getUserRole()))
                .toList();
    }

    public void joinCourseByCode(UUID requestingUserId, String code) {
        UserEntity requestingUserEntity = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        CourseEntity courseEntity = jpaCourseRepository.findByJoinCode(code)
                .orElseThrow(ExceptionUtility::courseNotFoundByCodeException);

        if (CourseUtility.getUserCourse(courseEntity, requestingUserEntity).isPresent()) {
            throw ExceptionUtility.userAlreadyParticipantInCourseException();
        }

        UserCourseEntity userCourseEntity = createUserCourseOnCourseJoin(courseEntity, requestingUserEntity);

        userCourseRepository.saveAndFlush(userCourseEntity);

        taskAnswerGeneralService.createTaskAnswersForNewCourseUser(requestingUserEntity, courseEntity);
    }

    public void changeUserRoleOnCourse(
            UUID requestingUserId,
            UUID courseId,
            UUID userId,
            UserCourseRole newUserRole
    ) {
        UserEntity requestingUserEntity = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        UserEntity userEntityToChange = userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        CourseEntity courseEntity = jpaCourseRepository.findById(courseId)
                .orElseThrow(ExceptionUtility::courseNotFoundException);

        if (!CourseUtility.isUserAvailableToChangeOtherUserRoleOnCourse(courseEntity, userEntityToChange, newUserRole, requestingUserEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        UserCourseEntity userCourseEntity = CourseUtility.getUserCourse(courseEntity, userEntityToChange)
                .orElseThrow(ExceptionUtility::userCourseNotFoundException);

        userCourseEntity.setUserRole(newUserRole);
        userCourseRepository.saveAndFlush(userCourseEntity);
    }

    public void removeUserFromCourse(
            UUID requestingUserId,
            UUID courseId,
            UUID userId
    ) {
        UserEntity requestingUserEntity = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        UserEntity userEntityToChange = userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        CourseEntity courseEntity = jpaCourseRepository.findById(courseId)
                .orElseThrow(ExceptionUtility::courseNotFoundException);

        if (!CourseUtility.isUserAvailableToRemoveOtherUserFromCourse(courseEntity, userEntityToChange, requestingUserEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        UserCourseEntity userCourseEntity = CourseUtility.getUserCourse(courseEntity, userEntityToChange)
                .orElseThrow(ExceptionUtility::userCourseNotFoundException);

        userCourseRepository.delete(userCourseEntity);
    }

    public void leaveCourse(UUID requestingUserId, UUID courseId) {
        UserEntity requestingUserEntity = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        CourseEntity courseEntity = jpaCourseRepository.findById(courseId)
                .orElseThrow(ExceptionUtility::courseNotFoundException);

        if (!CourseUtility.isUserAbleToLeaveCourse(courseEntity, requestingUserEntity)) {
            throw ExceptionUtility.userCannotLeaveCourseException();
        }

        UserCourseEntity userCourseEntity = CourseUtility.getUserCourse(courseEntity, requestingUserEntity)
                .orElseThrow(ExceptionUtility::forbiddenRightsException);

        userCourseRepository.delete(userCourseEntity);
    }

    private CourseEntity createCourseFromModel(CourseCreateModel courseCreateModel) {
        return new CourseEntity()
                .setId(UUID.randomUUID())
                .setName(courseCreateModel.getName())
                .setDescription(courseCreateModel.getDescription())
                .setIsArchived(false)
                .setJoinCode(courseCodeGenerator.generateNewCode())
                .setCreatedAt(LocalDateTime.now())
                .setCourseMarkEvaluationType(courseCreateModel.getCourseMarkEvaluationType())
                .setPassThreshold(courseCreateModel.getPassThreshold());
    }

    private UserCourseEntity createUserCourseOnCourseCreation(CourseEntity newCourseEntity, UserEntity creator) {
        return new UserCourseEntity()
                .setId(UUID.randomUUID())
                .setCourseEntity(newCourseEntity)
                .setUserEntity(creator)
                .setUserRole(UserCourseRole.HEAD_TEACHER)
                .setCreatedAt(LocalDateTime.now());
    }

    private UserCourseEntity createUserCourseOnCourseJoin(CourseEntity courseEntity, UserEntity joiningUserEntity) {
        return new UserCourseEntity()
                .setId(UUID.randomUUID())
                .setCourseEntity(courseEntity)
                .setUserEntity(joiningUserEntity)
                .setUserRole(UserCourseRole.STUDENT)
                .setCreatedAt(LocalDateTime.now());
    }

}
