package com.example.hits.cucumber.steps;

import com.example.hits.application.service.peer.PeerEvaluationService;
import com.example.hits.application.service.post.PostService;
import com.example.hits.application.service.taskanswer.TaskAnswerGeneralService;
import com.example.hits.application.service.taskanswer.TaskAnswerUploadService;
import com.example.hits.domain.entity.course.CourseMarkEvaluationType;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.domain.entity.post.TaskAnswerAppraisingType;
import com.example.hits.domain.entity.post.TaskMarkEvaluationType;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.repository.JpaCourseRepository;
import com.example.hits.infrastructure.persistence.entity.CourseEntity;
import com.example.hits.infrastructure.persistence.entity.PostEntity;
import com.example.hits.infrastructure.persistence.entity.UserCourseEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import com.example.hits.infrastructure.persistence.repository.FileRepository;
import com.example.hits.infrastructure.persistence.repository.JpaCriteriaScoreRepository;
import com.example.hits.infrastructure.persistence.repository.JpaTaskAnswerRepository;
import com.example.hits.infrastructure.persistence.repository.JpaTaskAnswerStudentAppraiserRepository;
import com.example.hits.infrastructure.persistence.repository.PostCommentRepository;
import com.example.hits.infrastructure.persistence.repository.PostRepository;
import com.example.hits.infrastructure.persistence.repository.TaskAnswerCommentRepository;
import com.example.hits.infrastructure.persistence.repository.UserRepository;
import com.example.hits.presentation.request.post.PostCreateModel;
import com.example.hits.presentation.request.post.PostUpdateModel;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PostAppraisingConfigSteps {

    private JpaCourseRepository jpaCourseRepository;
    private PostRepository postRepository;
    private UserRepository userRepository;
    private PostService postService;

    private CourseEntity course;
    private UserEntity teacher;
    private PostEntity existingPost;
    private RuntimeException exception;

    @Before
    public void setUp() {
        TaskAnswerGeneralService taskAnswerGeneralService = mock(TaskAnswerGeneralService.class);
        TaskAnswerUploadService taskAnswerUploadService = mock(TaskAnswerUploadService.class);
        PeerEvaluationService peerEvaluationService = mock(PeerEvaluationService.class);
        jpaCourseRepository = mock(JpaCourseRepository.class);
        postRepository = mock(PostRepository.class);
        userRepository = mock(UserRepository.class);
        FileRepository fileRepository = mock(FileRepository.class);
        PostCommentRepository postCommentRepository = mock(PostCommentRepository.class);
        JpaTaskAnswerRepository jpaTaskAnswerRepository = mock(JpaTaskAnswerRepository.class);
        TaskAnswerCommentRepository taskAnswerCommentRepository = mock(TaskAnswerCommentRepository.class);
        JpaTaskAnswerStudentAppraiserRepository jpaAppraiserRepository = mock(JpaTaskAnswerStudentAppraiserRepository.class);
        JpaCriteriaScoreRepository jpaCriteriaScoreRepository = mock(JpaCriteriaScoreRepository.class);

        postService = new PostService(
                taskAnswerGeneralService,
                taskAnswerUploadService,
                peerEvaluationService,
                jpaCourseRepository,
                postRepository,
                userRepository,
                fileRepository,
                postCommentRepository,
                jpaTaskAnswerRepository,
                taskAnswerCommentRepository,
                jpaAppraiserRepository,
                jpaCriteriaScoreRepository);

        Mockito.lenient().when(postRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.lenient().when(jpaTaskAnswerRepository.findAllByPostEntityId(any())).thenReturn(List.of());
        exception = null;
    }

    @Given("a teacher can edit a course")
    public void aTeacherCanEditACourse() {
        teacher = new UserEntity()
                .setId(UUID.randomUUID())
                .setFirstName("Teacher")
                .setLastName("One")
                .setEmail("teacher@test.com")
                .setBirthday(LocalDate.of(1980, 1, 1))
                .setCity("Moscow")
                .setPasswordHash("hash")
                .setCreatedAt(LocalDateTime.now());

        course = new CourseEntity()
                .setId(UUID.randomUUID())
                .setName("Test Course")
                .setDescription("Test")
                .setJoinCode("TEST1234")
                .setCourseMarkEvaluationType(CourseMarkEvaluationType.SUM)
                .setIsArchived(false)
                .setCreatedAt(LocalDateTime.now());

        var userCourse = new UserCourseEntity()
                .setId(UUID.randomUUID())
                .setCourseEntity(course)
                .setUserEntity(teacher)
                .setUserRole(UserCourseRole.TEACHER)
                .setCreatedAt(LocalDateTime.now());
        course.setCourseUsers(List.of(userCourse));

        when(jpaCourseRepository.findById(course.getId())).thenReturn(Optional.of(course));
        when(userRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
    }

    @Given("an existing ANY appraising task with student appraising limit {int}")
    public void anExistingAnyAppraisingTaskWithStudentAppraisingLimit(int limit) {
        existingPost = createExistingPost(limit);
        when(postRepository.findById(existingPost.getId())).thenReturn(Optional.of(existingPost));
    }

    @When("teacher creates an ANY appraising task with student appraising limit {int}")
    public void teacherCreatesAnAnyAppraisingTaskWithStudentAppraisingLimit(int limit) {
        postService.createPost(course.getId(), teacher.getId(), createPostModel(limit));
    }

    @When("teacher tries to create an ANY appraising task with student appraising limit {int}")
    public void teacherTriesToCreateAnAnyAppraisingTaskWithStudentAppraisingLimit(int limit) {
        try {
            postService.createPost(course.getId(), teacher.getId(), createPostModel(limit));
        } catch (RuntimeException e) {
            exception = e;
        }
    }

    @When("teacher updates the student appraising limit to {int}")
    public void teacherUpdatesTheStudentAppraisingLimitTo(int limit) {
        postService.updatePost(course.getId(), existingPost.getId(), teacher.getId(), updatePostModel(limit));
    }

    @Then("saved task has student appraising limit {int}")
    public void savedTaskHasStudentAppraisingLimit(int limit) {
        var captor = ArgumentCaptor.forClass(PostEntity.class);
        verify(postRepository, Mockito.atLeastOnce()).save(captor.capture());
        var savedPost = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals(limit, savedPost.getStudentAppraisingNumber());
    }

    @Then("updated task has student appraising limit {int}")
    public void updatedTaskHasStudentAppraisingLimit(int limit) {
        assertEquals(limit, existingPost.getStudentAppraisingNumber());
        verify(postRepository).save(existingPost);
    }

    @Then("post appraising configuration is rejected")
    public void postAppraisingConfigurationIsRejected() {
        assertNotNull(exception);
    }

    private PostCreateModel createPostModel(int studentAppraisingNumber) {
        var deadline = LocalDateTime.now().plusDays(1);
        return new PostCreateModel(
                "Peer evaluation task",
                List.of(),
                PostType.TASK,
                TaskMarkEvaluationType.SUM,
                10f,
                0f,
                null,
                null,
                null,
                deadline,
                deadline.plusDays(1),
                studentAppraisingNumber,
                TaskAnswerAppraisingType.ANY,
                true,
                true);
    }

    private PostUpdateModel updatePostModel(int studentAppraisingNumber) {
        return new PostUpdateModel(
                "Updated peer evaluation task",
                List.of(),
                TaskMarkEvaluationType.SUM,
                10f,
                0f,
                null,
                null,
                null,
                existingPost.getDeadline().plusDays(1),
                studentAppraisingNumber,
                TaskAnswerAppraisingType.ANY,
                true,
                true);
    }

    private PostEntity createExistingPost(int studentAppraisingNumber) {
        var deadline = LocalDateTime.now().plusDays(1);
        return new PostEntity()
                .setId(UUID.randomUUID())
                .setText("Peer evaluation task")
                .setCourseEntity(course)
                .setAuthor(teacher)
                .setPostType(PostType.TASK)
                .setTaskMarkEvaluationType(TaskMarkEvaluationType.SUM)
                .setMinScore(0f)
                .setMaxScore(10f)
                .setDeadline(deadline)
                .setAppraiserDeadline(deadline.plusDays(1))
                .setStudentAppraisingNumber(studentAppraisingNumber)
                .setTaskAnswerAppraisingType(TaskAnswerAppraisingType.ANY)
                .setCanSeeAppraiser(true)
                .setCanSeeAppraised(true)
                .setFileEntities(List.of())
                .setComments(List.of())
                .setCreatedAt(LocalDateTime.now());
    }
}
