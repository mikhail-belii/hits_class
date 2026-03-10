package com.example.hits.domain.service.taskanswer;

import com.example.hits.application.model.taskanswer.TaskRateRequestModel;
import com.example.hits.application.repository.TaskAnswerRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskAnswerUploadServiceTests {

    @Mock
    private TaskAnswerRepository taskAnswerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskAnswerUploadService taskAnswerUploadService;

    @Test
    void evaluateTask_whenUserIsTeacherAndScoreIsValid_updatesScoreAndSavesTaskAnswer() {
        UUID taskAnswerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User().setId(userId);
        Course course = new Course();
        course.setCourseUsers(List.of(new UserCourse()
                .setUser(user)
                .setCourse(course)
                .setUserRole(UserCourseRole.TEACHER)));
        Post post = new Post().setCourse(course).setMaxScore(10);
        TaskAnswer taskAnswer = new TaskAnswer().setId(taskAnswerId).setPost(post).setScore(0);
        TaskRateRequestModel taskRate = new TaskRateRequestModel();
        taskRate.setRate(7);

        when(taskAnswerRepository.findById(taskAnswerId)).thenReturn(Optional.of(taskAnswer));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        taskAnswerUploadService.evaluateTask(taskAnswerId, taskRate, userId);

        assertEquals(7, taskAnswer.getScore());
        verify(taskAnswerRepository).save(argThat(savedTaskAnswer ->
                savedTaskAnswer.getId().equals(taskAnswerId) && savedTaskAnswer.getScore() == 7));
    }

    @Test
    void evaluateTask_whenUserIsNotTeacher_throwsForbiddenRightsException() {
        UUID taskAnswerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User().setId(userId);
        Course course = new Course();
        course.setCourseUsers(List.of(new UserCourse()
                .setUser(user)
                .setCourse(course)
                .setUserRole(UserCourseRole.STUDENT)));
        Post post = new Post().setCourse(course).setMaxScore(10);
        TaskAnswer taskAnswer = new TaskAnswer().setId(taskAnswerId).setPost(post).setScore(0);
        TaskRateRequestModel taskRate = new TaskRateRequestModel();
        taskRate.setRate(7);

        when(taskAnswerRepository.findById(taskAnswerId)).thenReturn(Optional.of(taskAnswer));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ExceptionUtility.forbiddenRightsException().getClass(),
                () -> taskAnswerUploadService.evaluateTask(taskAnswerId, taskRate, userId));
    }

    @Test
    void evaluateTask_whenScoreExceedsMaxScore_throwsBadRequestException() {
        UUID taskAnswerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User().setId(userId);
        Course course = new Course();
        course.setCourseUsers(List.of(new UserCourse()
                .setUser(user)
                .setCourse(course)
                .setUserRole(UserCourseRole.TEACHER)));
        Post post = new Post().setCourse(course).setMaxScore(5);
        TaskAnswer taskAnswer = new TaskAnswer().setId(taskAnswerId).setPost(post).setScore(0);
        TaskRateRequestModel taskRate = new TaskRateRequestModel();
        taskRate.setRate(7);

        when(taskAnswerRepository.findById(taskAnswerId)).thenReturn(Optional.of(taskAnswer));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ExceptionUtility.badRequestException("Invalid score").getClass(),
                () -> taskAnswerUploadService.evaluateTask(taskAnswerId, taskRate, userId));
    }

    @Test
    void evaluateTask_whenTaskAnswerDoesNotExist_throwsTaskAnswerNotFoundException() {
        UUID taskAnswerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TaskRateRequestModel taskRate = new TaskRateRequestModel();
        taskRate.setRate(7);

        when(taskAnswerRepository.findById(taskAnswerId)).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.taskAnswerNotFoundException().getClass(),
                () -> taskAnswerUploadService.evaluateTask(taskAnswerId, taskRate, userId));
    }

    @Test
    void evaluateTask_whenUserDoesNotExist_throwsUserNotFoundException() {
        UUID taskAnswerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TaskAnswer taskAnswer = new TaskAnswer()
                .setId(taskAnswerId)
                .setPost(new Post().setCourse(new Course()).setMaxScore(10));
        TaskRateRequestModel taskRate = new TaskRateRequestModel();
        taskRate.setRate(7);

        when(taskAnswerRepository.findById(taskAnswerId)).thenReturn(Optional.of(taskAnswer));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.userNotFoundException().getClass(),
                () -> taskAnswerUploadService.evaluateTask(taskAnswerId, taskRate, userId));
    }
}
