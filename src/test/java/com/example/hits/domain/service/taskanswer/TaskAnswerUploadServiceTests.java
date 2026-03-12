package com.example.hits.domain.service.taskanswer;

import com.example.hits.application.model.file.FileModel;
import com.example.hits.application.model.taskanswer.TaskRateRequestModel;
import com.example.hits.application.repository.FileRepository;
import com.example.hits.application.repository.TaskAnswerRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.file.File;
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

import java.time.LocalDateTime;
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

    @Mock
    private FileRepository fileRepository;

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
        TaskAnswer taskAnswer = new TaskAnswer().setId(taskAnswerId).setPost(post).setScore(1);
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
        TaskAnswer taskAnswer = new TaskAnswer().setId(taskAnswerId).setPost(post).setScore(1);
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
        TaskAnswer taskAnswer = new TaskAnswer().setId(taskAnswerId).setPost(post).setScore(1);
        TaskRateRequestModel taskRate = new TaskRateRequestModel();
        taskRate.setRate(7);

        when(taskAnswerRepository.findById(taskAnswerId)).thenReturn(Optional.of(taskAnswer));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ExceptionUtility.badRequestException("Invalid score").getClass(),
                () -> taskAnswerUploadService.evaluateTask(taskAnswerId, taskRate, userId));
    }

    @Test
    void evaluateTask_whenCurrentTaskAnswerScoreIsNegative_throwsBadRequestException() {
        UUID taskAnswerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User().setId(userId);
        Course course = new Course();
        course.setCourseUsers(List.of(new UserCourse()
                .setUser(user)
                .setCourse(course)
                .setUserRole(UserCourseRole.TEACHER)));
        Post post = new Post().setCourse(course).setMaxScore(10);
        TaskAnswer taskAnswer = new TaskAnswer().setId(taskAnswerId).setPost(post).setScore(-1);
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

    @Test
    void submitTask_whenUserIsTaskAnswerAuthor_setsSubmittedAtAndSavesTaskAnswer() {
        UUID taskAnswerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User().setId(userId);
        TaskAnswer taskAnswer = new TaskAnswer()
                .setId(taskAnswerId)
                .setUser(user)
                .setSubmittedAt(null);

        when(taskAnswerRepository.findById(taskAnswerId)).thenReturn(Optional.of(taskAnswer));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        taskAnswerUploadService.submitTask(taskAnswerId, userId);

        verify(taskAnswerRepository).save(argThat(savedTaskAnswer ->
                savedTaskAnswer.getId().equals(taskAnswerId)
                        && savedTaskAnswer.getSubmittedAt() != null));
    }

    @Test
    void submitTask_whenUserIsNotTaskAnswerAuthor_throwsForbiddenRightsException() {
        UUID taskAnswerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User author = new User().setId(UUID.randomUUID());
        User anotherUser = new User().setId(userId);
        TaskAnswer taskAnswer = new TaskAnswer()
                .setId(taskAnswerId)
                .setUser(author);

        when(taskAnswerRepository.findById(taskAnswerId)).thenReturn(Optional.of(taskAnswer));
        when(userRepository.findById(userId)).thenReturn(Optional.of(anotherUser));

        assertThrows(ExceptionUtility.forbiddenRightsException().getClass(),
                () -> taskAnswerUploadService.submitTask(taskAnswerId, userId));
    }

    @Test
    void submitTask_whenTaskAnswerDoesNotExist_throwsTaskAnswerNotFoundException() {
        UUID taskAnswerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(taskAnswerRepository.findById(taskAnswerId)).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.taskAnswerNotFoundException().getClass(),
                () -> taskAnswerUploadService.submitTask(taskAnswerId, userId));
    }

    @Test
    void submitTask_whenUserDoesNotExist_throwsUserNotFoundException() {
        UUID taskAnswerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TaskAnswer taskAnswer = new TaskAnswer()
                .setId(taskAnswerId)
                .setUser(new User().setId(userId));

        when(taskAnswerRepository.findById(taskAnswerId)).thenReturn(Optional.of(taskAnswer));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.userNotFoundException().getClass(),
                () -> taskAnswerUploadService.submitTask(taskAnswerId, userId));
    }

    @Test
    void unsubmitTask_whenUserIsTaskAnswerAuthor_clearsSubmittedAtAndSavesTaskAnswer() {
        UUID taskAnswerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User().setId(userId);
        TaskAnswer taskAnswer = new TaskAnswer()
                .setId(taskAnswerId)
                .setUser(user)
                .setScore(0)
                .setSubmittedAt(LocalDateTime.now());

        when(taskAnswerRepository.findById(taskAnswerId)).thenReturn(Optional.of(taskAnswer));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        taskAnswerUploadService.unsubmitTask(taskAnswerId, userId);

        verify(taskAnswerRepository).save(argThat(savedTaskAnswer ->
                savedTaskAnswer.getId().equals(taskAnswerId)
                        && savedTaskAnswer.getSubmittedAt() == null));
    }

    @Test
    void unsubmitTask_whenUserIsNotTaskAnswerAuthor_throwsForbiddenRightsException() {
        UUID taskAnswerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User author = new User().setId(UUID.randomUUID());
        User anotherUser = new User().setId(userId);
        TaskAnswer taskAnswer = new TaskAnswer()
                .setId(taskAnswerId)
                .setUser(author)
                .setSubmittedAt(LocalDateTime.now());

        when(taskAnswerRepository.findById(taskAnswerId)).thenReturn(Optional.of(taskAnswer));
        when(userRepository.findById(userId)).thenReturn(Optional.of(anotherUser));

        assertThrows(ExceptionUtility.forbiddenRightsException().getClass(),
                () -> taskAnswerUploadService.unsubmitTask(taskAnswerId, userId));
    }

    @Test
    void unsubmitTask_whenTaskAnswerDoesNotExist_throwsTaskAnswerNotFoundException() {
        UUID taskAnswerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(taskAnswerRepository.findById(taskAnswerId)).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.taskAnswerNotFoundException().getClass(),
                () -> taskAnswerUploadService.unsubmitTask(taskAnswerId, userId));
    }

    @Test
    void unsubmitTask_whenUserDoesNotExist_throwsUserNotFoundException() {
        UUID taskAnswerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        TaskAnswer taskAnswer = new TaskAnswer()
                .setId(taskAnswerId)
                .setUser(new User().setId(userId))
                .setScore(0)
                .setSubmittedAt(LocalDateTime.now());

        when(taskAnswerRepository.findById(taskAnswerId)).thenReturn(Optional.of(taskAnswer));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.userNotFoundException().getClass(),
                () -> taskAnswerUploadService.unsubmitTask(taskAnswerId, userId));
    }

    @Test
    void unsubmitTask_whenTaskAlreadyEvaluated_throwsBadRequestException() {
        UUID taskAnswerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = new User().setId(userId);
        TaskAnswer taskAnswer = new TaskAnswer()
                .setId(taskAnswerId)
                .setUser(user)
                .setScore(5)
                .setSubmittedAt(LocalDateTime.now());

        when(taskAnswerRepository.findById(taskAnswerId)).thenReturn(Optional.of(taskAnswer));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ExceptionUtility.badRequestException("Task already evaluated").getClass(),
                () -> taskAnswerUploadService.unsubmitTask(taskAnswerId, userId));
    }

    @Test
    void appendFiles_whenFilesExistAndUserIsOwner_setsFilesAndSavesTaskAnswer() {
        UUID taskAnswerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID firstFileId = UUID.randomUUID();
        UUID secondFileId = UUID.randomUUID();
        User user = new User().setId(userId);
        File firstFile = new File().setId(firstFileId);
        File secondFile = new File().setId(secondFileId);
        TaskAnswer taskAnswer = new TaskAnswer()
                .setId(taskAnswerId)
                .setUser(user)
                .setSubmittedAt(null);

        when(taskAnswerRepository.findById(taskAnswerId)).thenReturn(Optional.of(taskAnswer));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(fileRepository.findAllById(List.of(firstFileId, secondFileId))).thenReturn(List.of(firstFile, secondFile));

        taskAnswerUploadService.appendFiles(
                taskAnswerId,
                List.of(new FileModel(firstFileId, "name"), new FileModel(secondFileId, "name")),
                userId
        );

        assertEquals(2, taskAnswer.getFiles().size());
        assertEquals(firstFileId, taskAnswer.getFiles().get(0).getId());
        assertEquals(secondFileId, taskAnswer.getFiles().get(1).getId());
        verify(taskAnswerRepository).save(taskAnswer);
    }

    @Test
    void unpinFiles_whenFileExistsInTaskAnswerFiles_removesFileAndSavesTaskAnswer() {
        UUID taskAnswerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID removableFileId = UUID.randomUUID();
        User user = new User().setId(userId);
        File removableFile = new File().setId(removableFileId);
        File anotherFile = new File().setId(UUID.randomUUID());
        TaskAnswer taskAnswer = new TaskAnswer()
                .setId(taskAnswerId)
                .setUser(user)
                .setSubmittedAt(null)
                .setFiles(new java.util.ArrayList<>(List.of(removableFile, anotherFile)));

        when(taskAnswerRepository.findById(taskAnswerId)).thenReturn(Optional.of(taskAnswer));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        taskAnswerUploadService.unpinFiles(taskAnswerId, removableFileId, userId);

        assertEquals(1, taskAnswer.getFiles().size());
        assertEquals(anotherFile.getId(), taskAnswer.getFiles().getFirst().getId());
        verify(taskAnswerRepository).save(taskAnswer);
    }
}
