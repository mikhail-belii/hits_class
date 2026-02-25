package com.example.hits.domain.service.course;

import com.example.hits.application.model.course.CourseCreateModel;
import com.example.hits.application.model.course.CourseEditModel;
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
    private Course course;
    private CourseEditModel editModel;
    private CourseCreateModel createModel;

    @BeforeEach
    public void init () {
        user = createUser(UUID.randomUUID());
        course = createCourse(UUID.randomUUID(), "Course Name", "Course Desc", false);
        course.setCourseUsers(List.of(createUserCourse(user, course, UserCourseRole.HEAD_TEACHER)));
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


}
