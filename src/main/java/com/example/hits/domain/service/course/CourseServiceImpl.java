package com.example.hits.domain.service.course;

import com.example.hits.application.model.course.*;
import com.example.hits.application.repository.CourseRepository;
import com.example.hits.application.repository.UserCourseRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.application.service.CourseService;
import com.example.hits.application.util.CourseUtility;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import com.example.hits.domain.mapper.CourseMapper;
import com.example.hits.domain.mapper.UserCourseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;
    private final CourseCodeGenerator courseCodeGenerator;

    @Transactional
    public CourseModel createCourse(UUID requestingUserId, CourseCreateModel courseCreateModel) {
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        Course course = createCourseFromModel(courseCreateModel);
        UserCourse userCourse = createUserCourseOnCourseCreation(course, requestingUser);

        courseRepository.save(course);
        userCourseRepository.save(userCourse);

        courseRepository.flush();

        return CourseMapper.toModel(course, userCourse.getUserRole());
    }

    public void editCourse(UUID requestingUserId, UUID courseId, CourseEditModel courseEditModel) {
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        Course editingCourse = courseRepository.findById(courseId)
                .orElseThrow(ExceptionUtility::courseNotFoundException);

        if (!CourseUtility.isCourseAvailableForEditing(editingCourse, requestingUser)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        editingCourse
            .setName(courseEditModel.getName())
            .setDescription(courseEditModel.getDescription());

        courseRepository.saveAndFlush(editingCourse);
    }

    public void archiveCourse(UUID requestingUserId, boolean isArchived, UUID courseId){
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        Course editingCourse = courseRepository.findById(courseId)
                .orElseThrow(ExceptionUtility::courseNotFoundException);

        if (!CourseUtility.isCourseAvailableForEditing(editingCourse, requestingUser)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        editingCourse.setIsArchived(isArchived);

        courseRepository.saveAndFlush(editingCourse);
    }

    public List<UserCourseModel> getCourseUsers(UUID requestingUserId, UUID courseId) {
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(ExceptionUtility::courseNotFoundException);

        if (CourseUtility.getUserCourse(course, requestingUser).isEmpty()) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        return course.getCourseUsers()
                .stream()
                .map(UserCourseMapper::toModel)
                .toList();
    }

    public CourseModel getConcreteCourse(UUID requestingUserId, UUID courseId) {
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(ExceptionUtility::courseNotFoundException);

        UserCourse userCourse = CourseUtility.getUserCourse(course, requestingUser)
                .orElseThrow(ExceptionUtility::forbiddenRightsException);


        return CourseMapper.toModel(course, userCourse.getUserRole());
    }

    public List<CourseShortModel> getUserCourses(UUID requestingUserId, boolean isArchived) {
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);

        return requestingUser.getUserCourses()
                .stream()
                .filter(c -> c.getCourse().getIsArchived() == isArchived)
                .map(c -> CourseMapper.toShortModel(c.getCourse()))
                .toList();
    }

    public void joinCourseByCode(UUID requestingUserId, String code) {
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        Course course = courseRepository.findByJoinCode(code)
                .orElseThrow(ExceptionUtility::courseNotFoundByCodeException);

        if (CourseUtility.getUserCourse(course, requestingUser).isPresent()) {
            throw ExceptionUtility.userAlreadyParticipantInCourseException();
        }

        UserCourse userCourse = createUserCourseOnCourseJoin(course, requestingUser);

        userCourseRepository.saveAndFlush(userCourse);
    }

    public void changeUserRoleOnCourse(
            UUID requestingUserId,
            UUID courseId,
            UUID userId,
            UserCourseRole newUserRole
    ) {
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        User userToChange = userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(ExceptionUtility::courseNotFoundException);

        if (!CourseUtility.isUserAvailableToChangeOtherUserRoleOnCourse(course, userToChange, newUserRole, requestingUser)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        UserCourse userCourse = CourseUtility.getUserCourse(course, userToChange)
                .orElseThrow(ExceptionUtility::userCourseNotFoundException);

        userCourse.setUserRole(newUserRole);
        userCourseRepository.saveAndFlush(userCourse);
    }

    public void removeUserFromCourse(
            UUID requestingUserId,
            UUID courseId,
            UUID userId
    ) {
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        User userToChange = userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(ExceptionUtility::courseNotFoundException);

        if (!CourseUtility.isUserAvailableToRemoveOtherUserFromCourse(course, userToChange, requestingUser)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        UserCourse userCourse = CourseUtility.getUserCourse(course, userToChange)
                .orElseThrow(ExceptionUtility::userCourseNotFoundException);

        userCourseRepository.delete(userCourse);
    }

    private Course createCourseFromModel(CourseCreateModel courseCreateModel) {
        return new Course()
                .setId(UUID.randomUUID())
                .setName(courseCreateModel.getName())
                .setDescription(courseCreateModel.getDescription())
                .setIsArchived(false)
                .setJoinCode(courseCodeGenerator.generateNewCode())
                .setCreatedAt(LocalDateTime.now());
    }

    private UserCourse createUserCourseOnCourseCreation(Course newCourse, User creator) {
        return new UserCourse()
                .setId(UUID.randomUUID())
                .setCourse(newCourse)
                .setUser(creator)
                .setUserRole(UserCourseRole.HEAD_TEACHER)
                .setCreatedAt(LocalDateTime.now());
    }

    private UserCourse createUserCourseOnCourseJoin(Course course, User joiningUser) {
        return new UserCourse()
                .setId(UUID.randomUUID())
                .setCourse(course)
                .setUser(joiningUser)
                .setUserRole(UserCourseRole.STUDENT)
                .setCreatedAt(LocalDateTime.now());
    }

}
