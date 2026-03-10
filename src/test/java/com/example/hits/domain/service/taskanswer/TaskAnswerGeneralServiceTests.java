package com.example.hits.domain.service.taskanswer;

import com.example.hits.application.repository.TaskAnswerRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.taskanswer.TaskAnswerStatus;
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
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskAnswerGeneralServiceTests {

    @Mock
    private TaskAnswerRepository taskAnswerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskAnswerGeneralService taskAnswerGeneralService;

    @Test
    void createTaskAnswerForEveryCourseMember_courseUsersIsNull_notSavingData() {
        Course course = new Course().setCourseUsers(null);
        Post post = new Post().setId(UUID.randomUUID());

        taskAnswerGeneralService.createTaskAnswerForEveryCourseMember(course, post);

        verifyNoInteractions(taskAnswerRepository);
    }

    @Test
    void createTaskAnswerForEveryCourseMember_courseUsersIsEmpty_notSavingData() {
        Course course = new Course().setCourseUsers(List.of());
        Post post = new Post().setId(UUID.randomUUID());

        taskAnswerGeneralService.createTaskAnswerForEveryCourseMember(course, post);

        verifyNoInteractions(taskAnswerRepository);
    }

    @Test
    void createTaskAnswerForEveryCourseMember_validCourseUsers_savesTaskAnswersForEveryMember() {
        User firstUser = new User().setId(UUID.randomUUID());
        User secondUser = new User().setId(UUID.randomUUID());
        UserCourse firstUserCourse = new UserCourse().setUser(firstUser);
        UserCourse secondUserCourse = new UserCourse().setUser(secondUser);
        Course course = new Course().setCourseUsers(List.of(firstUserCourse, secondUserCourse));
        Post post = new Post().setId(UUID.randomUUID());

        taskAnswerGeneralService.createTaskAnswerForEveryCourseMember(course, post);

        verify(taskAnswerRepository).saveAll(argThat(taskAnswers -> {
            var savedTaskAnswers = StreamSupport.stream(taskAnswers.spliterator(), false)
                    .toList();

            return savedTaskAnswers.size() == 2
                    && savedTaskAnswers.stream().allMatch(answer -> post.equals(answer.getPost()))
                    && savedTaskAnswers.stream().map(TaskAnswer::getUser).toList()
                    .equals(List.of(firstUser, secondUser));
        }));
    }

    @Test
    void createTaskAnswerForUser_validPostAndUser_savesTaskAnswer() {
        User user = new User().setId(UUID.randomUUID());
        Post post = new Post().setId(UUID.randomUUID());

        taskAnswerGeneralService.createTaskAnswerForUser(post, user);

        verify(taskAnswerRepository).save(argThat(taskAnswer ->
                post.equals(taskAnswer.getPost())
                        && user.equals(taskAnswer.getUser())
        ));
    }

    @Test
    void getAllUserTaskAnswers_whenUserIsStudentOnCourse_returnsOnlyTaskAnswersFromThatCourse() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        User user = new User().setId(userId);
        Course course = new Course().setId(courseId);
        UserCourse studentCourse = new UserCourse()
                .setUser(user)
                .setCourse(course)
                .setUserRole(UserCourseRole.STUDENT);
        user.setUserCourses(List.of(studentCourse));

        TaskAnswer firstTaskAnswer = new TaskAnswer()
                .setId(UUID.randomUUID())
                .setUser(user)
                .setPost(new Post().setId(UUID.randomUUID()).setText("0123456789-first").setCourse(course))
                .setStatus(TaskAnswerStatus.NEW);
        TaskAnswer secondTaskAnswer = new TaskAnswer()
                .setId(UUID.randomUUID())
                .setUser(user)
                .setPost(new Post().setId(UUID.randomUUID()).setText("0123456789-second").setCourse(course))
                .setStatus(TaskAnswerStatus.COMPLETED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskAnswerRepository.findAllByUserIdAndPostCourseId(userId, courseId))
                .thenReturn(List.of(firstTaskAnswer, secondTaskAnswer));

        var result = taskAnswerGeneralService.getAllUserTaskAnswers(userId);

        assertEquals(2, result.size());
        assertEquals(firstTaskAnswer.getId(), result.get(0).getId());
        assertEquals(secondTaskAnswer.getId(), result.get(1).getId());
        assertEquals("0123456789", result.get(0).getPostName());
        assertEquals("0123456789", result.get(1).getPostName());
        verify(taskAnswerRepository).findAllByUserIdAndPostCourseId(userId, courseId);
    }

    @Test
    void getAllUserTaskAnswers_whenUserIsNotStudentOnCourse_doesNotRequestTaskAnswersForCourse() {
        UUID userId = UUID.randomUUID();
        User user = new User().setId(userId);
        Course course = new Course().setId(UUID.randomUUID());
        UserCourse teacherCourse = new UserCourse()
                .setUser(user)
                .setCourse(course)
                .setUserRole(UserCourseRole.TEACHER);
        user.setUserCourses(List.of(teacherCourse));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var result = taskAnswerGeneralService.getAllUserTaskAnswers(userId);

        assertEquals(0, result.size());
        verify(taskAnswerRepository, never()).findAllByUserIdAndPostCourseId(userId, course.getId());
    }

    @Test
    void getUserPostTaskAnswer_whenTaskAnswerExists_returnsMappedModel() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID taskAnswerId = UUID.randomUUID();
        TaskAnswer taskAnswer = new TaskAnswer()
                .setId(taskAnswerId)
                .setUser(new User().setId(userId))
                .setPost(new Post()
                        .setId(postId)
                        .setText("0123456789ГООООЛ")
                        .setCreatedAt(LocalDateTime.now().minusDays(2)))
                .setAttachments(List.of())
                .setComments(List.of());

        when(taskAnswerRepository.findByUserIdAndPostId(userId, postId))
                .thenReturn(Optional.of(taskAnswer));

        var result = taskAnswerGeneralService.getUserPostTaskAnswer(postId, userId);

        assertEquals(taskAnswerId, result.getId());
        assertEquals("0123456789", result.getPostName());
        assertEquals(TaskAnswerStatus.NEW, result.getStatus());
        verify(taskAnswerRepository).findByUserIdAndPostId(userId, postId);
    }

    @Test
    void getUserPostTaskAnswer_whenTaskAnswerDoesNotExist_throwsTaskAnswerNotFoundException() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        when(taskAnswerRepository.findByUserIdAndPostId(userId, postId))
                .thenReturn(Optional.empty());

        assertThrows(ExceptionUtility.taskAnswerNotFoundException().getClass(),
                () -> taskAnswerGeneralService.getUserPostTaskAnswer(postId, userId));
    }
}
