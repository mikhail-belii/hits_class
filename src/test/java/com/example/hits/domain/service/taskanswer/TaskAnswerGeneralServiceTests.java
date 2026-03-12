package com.example.hits.domain.service.taskanswer;

import com.example.hits.application.repository.PostRepository;
import com.example.hits.application.repository.TaskAnswerRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.post.PostType;
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
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private PostRepository postRepository;

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
    void createTaskAnswersForNewCourseUser_whenCourseHasTaskPosts_savesTaskAnswerForEachTaskPost() {
        User user = new User().setId(UUID.randomUUID());
        Course course = new Course().setId(UUID.randomUUID());
        Post firstTaskPost = new Post().setId(UUID.randomUUID()).setCourse(course).setPostType(PostType.TASK);
        Post secondTaskPost = new Post().setId(UUID.randomUUID()).setCourse(course).setPostType(PostType.TASK);

        when(postRepository.findAllByCourseAndPostType(course, PostType.TASK))
                .thenReturn(List.of(firstTaskPost, secondTaskPost));
        when(taskAnswerRepository.findByUserIdAndPostId(user.getId(), firstTaskPost.getId()))
                .thenReturn(Optional.empty());
        when(taskAnswerRepository.findByUserIdAndPostId(user.getId(), secondTaskPost.getId()))
                .thenReturn(Optional.empty());

        taskAnswerGeneralService.createTaskAnswersForNewCourseUser(user, course);

        verify(postRepository).findAllByCourseAndPostType(course, PostType.TASK);
        verify(taskAnswerRepository).findByUserIdAndPostId(user.getId(), firstTaskPost.getId());
        verify(taskAnswerRepository).findByUserIdAndPostId(user.getId(), secondTaskPost.getId());
        verify(taskAnswerRepository).save(argThat(taskAnswer ->
                firstTaskPost.equals(taskAnswer.getPost()) && user.equals(taskAnswer.getUser())));
        verify(taskAnswerRepository).save(argThat(taskAnswer ->
                secondTaskPost.equals(taskAnswer.getPost()) && user.equals(taskAnswer.getUser())));
    }

    @Test
    void createTaskAnswersForNewCourseUser_whenTaskAnswerAlreadyExists_skipsExistingTaskPost() {
        User user = new User().setId(UUID.randomUUID());
        Course course = new Course().setId(UUID.randomUUID());
        Post firstTaskPost = new Post().setId(UUID.randomUUID()).setCourse(course).setPostType(PostType.TASK);
        Post secondTaskPost = new Post().setId(UUID.randomUUID()).setCourse(course).setPostType(PostType.TASK);

        when(postRepository.findAllByCourseAndPostType(course, PostType.TASK))
                .thenReturn(List.of(firstTaskPost, secondTaskPost));
        when(taskAnswerRepository.findByUserIdAndPostId(user.getId(), firstTaskPost.getId()))
                .thenReturn(Optional.of(new TaskAnswer().setId(UUID.randomUUID()).setUser(user).setPost(firstTaskPost)));
        when(taskAnswerRepository.findByUserIdAndPostId(user.getId(), secondTaskPost.getId()))
                .thenReturn(Optional.empty());

        taskAnswerGeneralService.createTaskAnswersForNewCourseUser(user, course);

        verify(taskAnswerRepository).findByUserIdAndPostId(user.getId(), firstTaskPost.getId());
        verify(taskAnswerRepository).findByUserIdAndPostId(user.getId(), secondTaskPost.getId());
        verify(taskAnswerRepository, never()).save(argThat(taskAnswer ->
                firstTaskPost.equals(taskAnswer.getPost()) && user.equals(taskAnswer.getUser())));
        verify(taskAnswerRepository).save(argThat(taskAnswer ->
                secondTaskPost.equals(taskAnswer.getPost()) && user.equals(taskAnswer.getUser())));
    }

    @Test
    void createTaskAnswersForNewCourseUser_whenCourseHasNoTaskPosts_doesNotSaveTaskAnswers() {
        User user = new User().setId(UUID.randomUUID());
        Course course = new Course().setId(UUID.randomUUID());

        when(postRepository.findAllByCourseAndPostType(course, PostType.TASK))
                .thenReturn(List.of());

        taskAnswerGeneralService.createTaskAnswersForNewCourseUser(user, course);

        verify(postRepository).findAllByCourseAndPostType(course, PostType.TASK);
        verify(taskAnswerRepository, never()).findByUserIdAndPostId(any(UUID.class), any(UUID.class));
        verify(taskAnswerRepository, never()).save(any(TaskAnswer.class));
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
        assertEquals("0123456789-first", result.get(0).getPostName());
        assertEquals("0123456789-second", result.get(1).getPostName());
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
                .setFiles(List.of())
                .setComments(List.of());

        when(taskAnswerRepository.findByUserIdAndPostId(userId, postId))
                .thenReturn(Optional.of(taskAnswer));

        var result = taskAnswerGeneralService.getUserPostTaskAnswer(postId, userId);

        assertEquals(taskAnswerId, result.getId());
        assertEquals(postId, result.getPostId());
        assertEquals("0123456789ГООООЛ", result.getPostName());
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

    @Test
    void getAllPostTaskAnswers_whenUserIsTeacherOnPostCourse_returnsSubmittedTaskAnswersSortedByUserName() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        User user = new User().setId(userId);
        Course course = new Course().setId(courseId);
        UserCourse teacherOnCourse = new UserCourse()
                .setUser(user)
                .setCourse(course)
                .setUserRole(UserCourseRole.TEACHER);
        course.setCourseUsers(List.of(teacherOnCourse));

        Post post = new Post()
                .setId(postId)
                .setCourse(course)
                .setPostType(PostType.TASK);
        TaskAnswer firstTaskAnswer = new TaskAnswer()
                .setId(UUID.randomUUID())
                .setUser(new User()
                        .setId(UUID.randomUUID())
                        .setFirstName("Zoe")
                        .setLastName("Adams")
                        .setEmail("zoe@aaa.com")
                        .setCity("Tomsk")
                        .setBirthday(java.time.LocalDate.of(2000, 1, 1)))
                .setPost(new Post()
                        .setId(postId)
                        .setText("0123456789-first")
                        .setMaxScore(100)
                        .setCreatedAt(LocalDateTime.now().minusDays(2)))
                .setSubmittedAt(LocalDateTime.now())
                .setFiles(List.of())
                .setComments(List.of());
        TaskAnswer secondTaskAnswer = new TaskAnswer()
                .setId(UUID.randomUUID())
                .setUser(new User()
                        .setId(UUID.randomUUID())
                        .setFirstName("Alex")
                        .setLastName("Brown")
                        .setEmail("alex@aaa.com")
                        .setCity("Tomsk")
                        .setBirthday(java.time.LocalDate.of(2000, 1, 1)))
                .setPost(new Post()
                        .setId(postId)
                        .setText("short")
                        .setMaxScore(100)
                        .setCreatedAt(LocalDateTime.now().minusDays(10)))
                .setSubmittedAt(LocalDateTime.now())
                .setFiles(List.of())
                .setComments(List.of());
        TaskAnswer notSubmittedTaskAnswer = new TaskAnswer()
                .setId(UUID.randomUUID())
                .setUser(new User()
                        .setId(UUID.randomUUID())
                        .setFirstName("Bob")
                        .setLastName("Clark")
                        .setEmail("bob@aaa.com")
                        .setCity("Tomsk")
                        .setBirthday(java.time.LocalDate.of(2000, 1, 1)))
                .setPost(new Post()
                        .setId(postId)
                        .setText("ignored")
                        .setMaxScore(100)
                        .setCreatedAt(LocalDateTime.now().minusDays(1)))
                .setFiles(List.of())
                .setComments(List.of());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(taskAnswerRepository.findAllByPostId(postId))
                .thenReturn(List.of(firstTaskAnswer, secondTaskAnswer, notSubmittedTaskAnswer));

        var result = taskAnswerGeneralService.getAllPostTaskAnswers(postId, userId);

        assertEquals(2, result.size());
        assertEquals(secondTaskAnswer.getId(), result.get(0).getId());
        assertEquals(firstTaskAnswer.getId(), result.get(1).getId());
        assertEquals(postId, result.get(0).getPostId());
        assertEquals(postId, result.get(1).getPostId());
        assertEquals("Alex", result.get(0).getUser().getFirstName());
        assertEquals("Zoe", result.get(1).getUser().getFirstName());
        assertEquals("short", result.get(0).getPostName());
        assertEquals("0123456789-first", result.get(1).getPostName());
        verify(taskAnswerRepository).findAllByPostId(postId);
    }

    @Test
    void getAllPostTaskAnswers_whenUserIsNotTeacherOnPostCourse_throwsForbiddenRightsException() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        User user = new User().setId(userId);
        Course course = new Course().setId(UUID.randomUUID());
        UserCourse studentOnCourse = new UserCourse()
                .setUser(user)
                .setCourse(course)
                .setUserRole(UserCourseRole.STUDENT);
        course.setCourseUsers(List.of(studentOnCourse));
        Post post = new Post()
                .setId(postId)
                .setCourse(course)
                .setPostType(PostType.TASK);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        assertThrows(ExceptionUtility.forbiddenRightsException().getClass(),
                () -> taskAnswerGeneralService.getAllPostTaskAnswers(postId, userId));
    }

    @Test
    void getAllPostTaskAnswers_whenPostIsNotTask_throwsBadRequestException() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        User user = new User().setId(userId);
        Course course = new Course().setId(UUID.randomUUID());
        UserCourse teacherOnCourse = new UserCourse()
                .setUser(user)
                .setCourse(course)
                .setUserRole(UserCourseRole.TEACHER);
        course.setCourseUsers(List.of(teacherOnCourse));
        Post post = new Post()
                .setId(postId)
                .setCourse(course)
                .setPostType(PostType.ANNOUNCEMENT);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        assertThrows(ExceptionUtility.badRequestException("Post is not a task type").getClass(),
                () -> taskAnswerGeneralService.getAllPostTaskAnswers(postId, userId));
    }
}
