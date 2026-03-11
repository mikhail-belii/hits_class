package com.example.hits.domain.service.course;

import com.example.hits.application.model.course.*;
import com.example.hits.application.repository.CourseRepository;
import com.example.hits.application.repository.UserCourseRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.hits.domain.service.course.CourseServiceTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserCourseRepository userCourseRepository;
    @Mock
    private CourseCodeGenerator courseCodeGenerator;

    @InjectMocks
    private CourseServiceImpl courseService;

    private User user;
    private User userToChange;
    private Course course;
    private UserCourse userCourse;
    private UserCourse userToChangeCourse;
    private CourseEditModel editModel;
    private CourseCreateModel createModel;

    @BeforeEach
    public void init () {
        user = createUser(UUID.randomUUID());
        userToChange = createUser(UUID.randomUUID());
        course = createCourse(UUID.randomUUID(), "Course Name", "Course Desc", false);
        userCourse = createUserCourse(user, course, UserCourseRole.HEAD_TEACHER);
        user.setUserCourses(List.of(userCourse));
        userToChangeCourse = createUserCourse(userToChange, course, UserCourseRole.TEACHER);
        course.setCourseUsers(List.of(userCourse, userToChangeCourse));
        createModel = createCourseCreateModel("New Course", "New Course Description");
        editModel = createCourseEditModel("New Course", "New Course Description");
    }

    @Test
    void createCourse_whenUserExists_savesCourseAndUserCourse() {
        String generatedCode = "АбВг1234";

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseCodeGenerator.generateNewCode()).thenReturn(generatedCode);

        courseService.createCourse(user.getId(), createModel);

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(courseCaptor.capture());

        Course savedCourse = courseCaptor.getValue();
        assertEquals(createModel.getName(), savedCourse.getName());
        assertEquals(createModel.getDescription(), savedCourse.getDescription());
        assertEquals(generatedCode, savedCourse.getJoinCode());
        assertFalse(savedCourse.getIsArchived());
        assertNotNull(savedCourse.getCreatedAt());

        ArgumentCaptor<UserCourse> userCourseCaptor = ArgumentCaptor.forClass(UserCourse.class);
        verify(userCourseRepository).save(userCourseCaptor.capture());

        UserCourse savedUserCourse = userCourseCaptor.getValue();
        assertEquals(savedCourse, savedUserCourse.getCourse());
        assertEquals(user, savedUserCourse.getUser());
        assertEquals(UserCourseRole.HEAD_TEACHER, savedUserCourse.getUserRole());

        verify(courseRepository).flush();
    }

    @Test
    void createCourse_whenUserNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.userNotFoundException().getClass(),
                () -> courseService.createCourse(user.getId(), createModel));

        verify(courseRepository, times(0)).save(any());
        verify(userCourseRepository, times(0)).save(any());
    }

    @Test
    void createCourse_whenGeneratorThrowsException_throwsException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseCodeGenerator.generateNewCode()).thenThrow(new RuntimeException("Generator error"));

        assertThrows(RuntimeException.class,
                () -> courseService.createCourse(user.getId(), createModel));

        verify(courseRepository, never()).save(any());
        verify(userCourseRepository, never()).save(any());
    }

    @Test
    void editCourse_whenUserIsHeadTeacher_updatesCourseFields() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        courseService.editCourse(user.getId(), course.getId(), editModel);

        ArgumentCaptor<Course> courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).saveAndFlush(courseCaptor.capture());

        Course editedCourse = courseCaptor.getValue();
        assertEquals(editModel.getName(), editedCourse.getName());
        assertEquals(editModel.getDescription(), editedCourse.getDescription());
    }

    @Test
    void editCourse_whenUserNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.userNotFoundException().getClass(),
                () -> courseService.editCourse(user.getId(), course.getId(), editModel));

        verify(courseRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void editCourse_whenCourseNotFound_throwsCourseNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.courseNotFoundException().getClass(),
                () -> courseService.editCourse(user.getId(), course.getId(), editModel));

        verify(courseRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void editCourse_whenUserHasNoRights_throwsForbiddenRightsException() {
        course.getCourseUsers().getFirst().setUserRole(UserCourseRole.STUDENT);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        assertThrows(ExceptionUtility.forbiddenRightsException().getClass(),
                () -> courseService.editCourse(user.getId(), course.getId(), editModel));

        verify(courseRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void editCourse_whenUserNotInCourse_throwsForbiddenRightsException() {
        course.getCourseUsers().getFirst().setUser(new User());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        assertThrows(ExceptionUtility.forbiddenRightsException().getClass(),
                () -> courseService.editCourse(user.getId(), course.getId(), editModel));

        verify(courseRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void archiveCourse_whenTryingToArchive_updateCourse() {
        course.setIsArchived(false);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(courseRepository.saveAndFlush(any(Course.class))).thenReturn(course);

        courseService.archiveCourse(user.getId(), true, course.getId());

        assertEquals(true, course.getIsArchived());
        verify(courseRepository).saveAndFlush(course);
    }

    @Test
    void archiveCourse_whenTryingToUnarchive_updateCourse() {
        course.setIsArchived(true);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(courseRepository.saveAndFlush(any(Course.class))).thenReturn(course);

        courseService.archiveCourse(user.getId(), false, course.getId());

        assertEquals(false, course.getIsArchived());
        verify(courseRepository).saveAndFlush(course);
    }

    @Test
    void archiveCourse_whenUserNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.userNotFoundException().getClass(),
                () -> courseService.archiveCourse(user.getId(), true, course.getId()));

        verify(courseRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void archiveCourse_whenCourseNotFound_throwsCourseNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.courseNotFoundException().getClass(),
                () -> courseService.archiveCourse(user.getId(), true, course.getId()));

        verify(courseRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void archiveCourse_whenUserNotHaveEnoughRights_throwsForbiddenRightsException() {
        userCourse.setUserRole(UserCourseRole.STUDENT);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        assertThrows(ExceptionUtility.forbiddenRightsException().getClass(),
                () -> courseService.archiveCourse(user.getId(), true, course.getId()));

        verify(courseRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void changeUserRoleOnCourse_whenCanChangeRole_changeUserRole() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(userToChange.getId())).thenReturn(Optional.of(userToChange));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        courseService.changeUserRoleOnCourse(user.getId(), course.getId(), userToChange.getId(), UserCourseRole.TEACHER);

        assertEquals(UserCourseRole.TEACHER, userToChangeCourse.getUserRole());
        verify(userCourseRepository).saveAndFlush(userToChangeCourse);
    }

    @Test
    void changeUserRoleOnCourse_whenRequestingUserNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.userNotFoundException().getClass(),
                () -> courseService.changeUserRoleOnCourse(
                        user.getId(),
                        course.getId(),
                        userToChange.getId(),
                        UserCourseRole.TEACHER));

        verify(userCourseRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void changeUserRoleOnCourse_whenUserToChangeNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(userToChange.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.userNotFoundException().getClass(),
                () -> courseService.changeUserRoleOnCourse(
                        user.getId(),
                        course.getId(),
                        userToChange.getId(),
                        UserCourseRole.TEACHER));

        verify(userCourseRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void changeUserRoleOnCourse_whenCourseNotFound_throwsCourseNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(userToChange.getId())).thenReturn(Optional.of(userToChange));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.courseNotFoundException().getClass(),
                () -> courseService.changeUserRoleOnCourse(
                        user.getId(),
                        course.getId(),
                        userToChange.getId(),
                        UserCourseRole.TEACHER));

        verify(userCourseRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void changeUserRoleOnCourse_whenUserNotHaveEnoughRights_throwsForbiddenRightsException() {
        userCourse.setUserRole(UserCourseRole.TEACHER);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(userToChange.getId())).thenReturn(Optional.of(userToChange));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        assertThrows(ExceptionUtility.forbiddenRightsException().getClass(),
                () -> courseService.changeUserRoleOnCourse(
                        user.getId(),
                        course.getId(),
                        userToChange.getId(),
                        UserCourseRole.TEACHER));

        verify(userCourseRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void removeUserFromCourse_whenCanRemove_removeUserFromCourse() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(userToChange.getId())).thenReturn(Optional.of(userToChange));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        courseService.removeUserFromCourse(user.getId(), course.getId(), userToChange.getId());

        verify(userCourseRepository).delete(userToChangeCourse);
    }

    @Test
    void removeUserFromCourse_whenRequestingUserNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.userNotFoundException().getClass(),
                () -> courseService.removeUserFromCourse(user.getId(), course.getId(), userToChange.getId()));

        verify(userCourseRepository, times(0)).delete(any());
    }

    @Test
    void removeUserFromCourse_whenTargetUserNotFound_ThrowsException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(userToChange.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.userNotFoundException().getClass(),
                () -> courseService.removeUserFromCourse(user.getId(), course.getId(), userToChange.getId()));

        verify(userCourseRepository, times(0)).delete(any());
    }

    @Test
    void removeUserFromCourse_CourseNotFound_throwsCourseNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(userToChange.getId())).thenReturn(Optional.of(userToChange));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.courseNotFoundException().getClass(),
                () -> courseService.removeUserFromCourse(user.getId(), course.getId(), userToChange.getId()));

        verify(userCourseRepository, times(0)).delete(any());
    }

    @Test
    void removeUserFromCourse_whenUserNotHaveEnoughRights_throwsForbiddenRightsException() {
        userCourse.setUserRole(UserCourseRole.TEACHER);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findById(userToChange.getId())).thenReturn(Optional.of(userToChange));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        assertThrows(ExceptionUtility.forbiddenRightsException().getClass(),
                () -> courseService.removeUserFromCourse(user.getId(), course.getId(), userToChange.getId()));

        verify(userCourseRepository, times(0)).delete(any());
    }

    @Test
    void joinCourseByCode_whenCanJoin_joinCourse() {
        course.setCourseUsers(List.of());
        String joinCode = "тестКОД1";
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findByJoinCode(joinCode)).thenReturn(Optional.of(course));

        courseService.joinCourseByCode(user.getId(), joinCode);

        verify(userRepository, times(1)).findById(user.getId());
        verify(courseRepository, times(1)).findByJoinCode(joinCode);

        ArgumentCaptor<UserCourse> userCourseCaptor = ArgumentCaptor.forClass(UserCourse.class);
        verify(userCourseRepository, times(1)).saveAndFlush(userCourseCaptor.capture());
        UserCourse savedUserCourse = userCourseCaptor.getValue();
        assertEquals(UserCourseRole.STUDENT, savedUserCourse.getUserRole());
        assertEquals(course, savedUserCourse.getCourse());
        assertEquals(user, savedUserCourse.getUser());
    }

    @Test
    void joinCourseByCode_whenRequestingUserNotFound_throwsUserNotFoundException() {
        String joinCode = "тестКОД1";
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.userNotFoundException().getClass(),
                () -> courseService.joinCourseByCode(user.getId(), joinCode));

        verify(userCourseRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void joinCourseByCode_whenCourseNotFound_throwsCourseNotFoundException() {
        String joinCode = "тестКОД1";
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findByJoinCode(joinCode)).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.courseNotFoundByCodeException().getClass(),
                () -> courseService.joinCourseByCode(user.getId(), joinCode));

        verify(userCourseRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void joinCourseByCode_whenUserAlreadyParticipantInCourse_throwsUserAlreadyParticipantInCourseException() {
        String joinCode = "тестКОД1";
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findByJoinCode(joinCode)).thenReturn(Optional.of(course));

        assertThrows(ExceptionUtility.userAlreadyParticipantInCourseException().getClass(),
                () -> courseService.joinCourseByCode(user.getId(), joinCode));

        verify(userCourseRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void getCourseUsers_whenCanGetCourseUsers_returnCourseUsers() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        List<UserCourseModel> result = courseService.getCourseUsers(user.getId(), course.getId());

        assertEquals(2, result.size());
        assertEquals(user.getId(), result.get(0).getUserModel().getId());
        assertEquals(userToChange.getId(), result.get(1).getUserModel().getId());
    }

    @Test
    void getCourseUsers_whenRequestingUserNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.userNotFoundException().getClass(),
                () -> courseService.getCourseUsers(user.getId(), course.getId()));
    }

    @Test
    void getCourseUsers_whenCourseNotFound_throwsCourseNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.courseNotFoundException().getClass(),
                () -> courseService.getCourseUsers(user.getId(), course.getId()));
    }

    @Test
    void getCourseUsers_whenRequestingUserNotInCourse_throwsForbiddenRightsException() {
        course.setCourseUsers(List.of());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        assertThrows(ExceptionUtility.forbiddenRightsException().getClass(),
                () -> courseService.getCourseUsers(user.getId(), course.getId()));
    }

    @Test
    void getUserCourses_whenTryToGetArchivedCoursesAndUserHasArchivedCourses_returnUserArchivedCourses() {
        course.setIsArchived(true);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        List<CourseShortModel> result = courseService.getUserCourses(user.getId(), true);

        assertEquals(1, result.size());
        assertEquals(course.getId(), result.getFirst().getId());
    }

    @Test
    void getUserCourses_whenTryToGetArchivedCoursesAndUserHasNotArchivedCourses_returnListWithLengthZero() {
        user.setUserCourses(List.of());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        List<CourseShortModel> result = courseService.getUserCourses(user.getId(), true);

        assertEquals(0, result.size());
    }

    @Test
    void getUserCourses_whenTryToGetNotArchivedCoursesAndUserHasNotArchivedCourses_returnUserNotArchivedCourses() {
        course.setIsArchived(false);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        List<CourseShortModel> result = courseService.getUserCourses(user.getId(), false);

        assertEquals(1, result.size());
        assertEquals(course.getId(), result.getFirst().getId());
    }

    @Test
    void getUserCourses_whenTryToGetNotArchivedCoursesAndUserHasOnlyArchivedCourses_returnListWithLengthZero() {
        course.setIsArchived(true);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        List<CourseShortModel> result = courseService.getUserCourses(user.getId(), false);

        assertEquals(0, result.size());
    }

    @Test
    void getConcreteCourse_whenCanGetConcreteCourse_returnCourse() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        CourseModel result = courseService.getConcreteCourse(user.getId(), course.getId());

        assertEquals(course.getId(), result.getId());
    }

    @Test
    void getConcreteCourse_whenRequestingUserNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.userNotFoundException().getClass(),
                () -> courseService.getConcreteCourse(user.getId(), course.getId()));
    }

    @Test
    void getConcreteCourse_whenCourseNotFound_throwsCourseNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.courseNotFoundException().getClass(),
                () -> courseService.getConcreteCourse(user.getId(), course.getId()));
    }

    @Test
    void getConcreteCourse_whenRequestingUserNotInCourse_throwsForbiddenRightsException() {
        course.setCourseUsers(List.of());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        assertThrows(ExceptionUtility.forbiddenRightsException().getClass(),
                () -> courseService.getConcreteCourse(user.getId(), course.getId()));
    }

    @Test
    void leaveFromCourse_whenCanLeave_removeUserFromCourse() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        courseService.leaveFromCourse(user.getId(), course.getId());

        verify(userCourseRepository).delete(userCourse);
    }

    @Test
    void leaveFromCourse_whenRequestingUserNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.userNotFoundException().getClass(),
                () -> courseService.leaveFromCourse(user.getId(), course.getId()));

        verify(userCourseRepository, times(0)).delete(any());
    }

    @Test
    void leaveFromCourse_CourseNotFound_throwsCourseNotFoundException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.courseNotFoundException().getClass(),
                () -> courseService.leaveFromCourse(user.getId(), course.getId()));

        verify(userCourseRepository, times(0)).delete(any());
    }

    @Test
    void leaveFromCourse_whenUserNotInCourse_throwsUserCourseNotFoundException() {
        course.setCourseUsers(List.of());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        assertThrows(ExceptionUtility.userCourseNotFoundException().getClass(),
                () -> courseService.leaveFromCourse(user.getId(), course.getId()));

        verify(userCourseRepository, times(0)).delete(any());
    }

}
