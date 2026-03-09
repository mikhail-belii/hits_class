package com.example.hits.domain.service.taskanswer;

import com.example.hits.application.repository.TaskAnswerRepository;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.usercourse.UserCourse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class TaskAnswerServiceTests {

    @Mock
    private TaskAnswerRepository taskAnswerRepository;

    @InjectMocks
    private TaskAnswerService taskAnswerService;

    @Test
    void createTaskAnswerForEveryCourseMember_courseUsersIsNull_notSavingData() {
        Course course = new Course().setCourseUsers(null);
        Post post = new Post().setId(UUID.randomUUID());

        taskAnswerService.createTaskAnswerForEveryCourseMember(course, post);

        verifyNoInteractions(taskAnswerRepository);
    }

    @Test
    void createTaskAnswerForEveryCourseMember_courseUsersIsEmpty_notSavingData() {
        Course course = new Course().setCourseUsers(List.of());
        Post post = new Post().setId(UUID.randomUUID());

        taskAnswerService.createTaskAnswerForEveryCourseMember(course, post);

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

        taskAnswerService.createTaskAnswerForEveryCourseMember(course, post);

        verify(taskAnswerRepository).saveAll(argThat(taskAnswers -> {
            var savedTaskAnswers = StreamSupport.stream(taskAnswers.spliterator(), false)
                    .toList();

            return savedTaskAnswers.size() == 2
                    && savedTaskAnswers.stream().allMatch(answer -> post.equals(answer.getPost()))
                    && savedTaskAnswers.stream().map(TaskAnswer::getUser).toList()
                    .equals(List.of(firstUser, secondUser));
        }));
    }
}
