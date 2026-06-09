package com.example.hits.cucumber.steps;

import com.example.hits.application.service.peer.PeerEvaluationAvailabilityService;
import com.example.hits.application.service.taskanswer.TaskAnswerGeneralService;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.domain.entity.post.TaskAnswerAppraisingType;
import com.example.hits.domain.entity.post.TaskMarkEvaluationType;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.infrastructure.persistence.entity.CourseEntity;
import com.example.hits.infrastructure.persistence.entity.PostEntity;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerEntity;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerStudentAppraiserEntity;
import com.example.hits.infrastructure.persistence.entity.UserCourseEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import com.example.hits.infrastructure.persistence.repository.JpaTaskAnswerRepository;
import com.example.hits.infrastructure.persistence.repository.JpaTaskAnswerStudentAppraiserRepository;
import com.example.hits.infrastructure.persistence.repository.PostRepository;
import com.example.hits.infrastructure.persistence.repository.UserRepository;
import com.example.hits.presentation.dto.taskanswer.AvailablePeerEvaluationModel;
import com.example.hits.presentation.dto.taskanswer.PeerEvaluationUnavailableReason;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PeerEvaluationAvailableWorksSteps {

    private JpaTaskAnswerRepository jpaTaskAnswerRepository;
    private UserRepository userRepository;
    private PostRepository postRepository;
    private JpaTaskAnswerStudentAppraiserRepository jpaAppraiserRepository;
    private PeerEvaluationAvailabilityService peerEvaluationAvailabilityService;
    private TaskAnswerGeneralService taskAnswerGeneralService;

    private CourseEntity course;
    private PostEntity post;
    private Map<String, UserEntity> studentsByName;
    private Map<String, TaskAnswerEntity> answersByStudentName;
    private List<TaskAnswerStudentAppraiserEntity> appraiserEntities;
    private List<AvailablePeerEvaluationModel> availableWorks;

    @Before
    public void setUp() {
        jpaTaskAnswerRepository = mock(JpaTaskAnswerRepository.class);
        userRepository = mock(UserRepository.class);
        postRepository = mock(PostRepository.class);
        jpaAppraiserRepository = mock(JpaTaskAnswerStudentAppraiserRepository.class);
        peerEvaluationAvailabilityService = new PeerEvaluationAvailabilityService(
                jpaTaskAnswerRepository,
                userRepository,
                postRepository,
                jpaAppraiserRepository);

        taskAnswerGeneralService = new TaskAnswerGeneralService(
                jpaTaskAnswerRepository,
                userRepository,
                postRepository,
                jpaAppraiserRepository,
                peerEvaluationAvailabilityService);

        studentsByName = new LinkedHashMap<>();
        answersByStudentName = new LinkedHashMap<>();
        appraiserEntities = new ArrayList<>();
        availableWorks = List.of();
    }

    @Given("an ANY appraising task after submission deadline with students {string}, {string} and {string}")
    public void anyAppraisingTaskAfterSubmissionDeadlineWithStudents(String student1, String student2, String student3) {
        createAnyAppraisingTask(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), 2,
                List.of(student1, student2, student3));
    }

    @Given("an ANY appraising task after submission deadline with appraising limit {int} and students {string}, {string} and {string}")
    public void anyAppraisingTaskAfterSubmissionDeadlineWithLimitAndStudents(int limit,
                                                                             String student1,
                                                                             String student2,
                                                                             String student3) {
        createAnyAppraisingTask(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), limit,
                List.of(student1, student2, student3));
    }

    @Given("all students submitted their answers")
    public void allStudentsSubmittedTheirAnswers() {
        answersByStudentName.values().forEach(answer -> answer.setSubmittedAt(LocalDateTime.now().minusHours(1)));
    }

    @Given("student {string} already selected student {string} answer")
    public void studentAlreadySelectedStudentAnswer(String appraiserName, String appraisedName) {
        appraiserEntities.add(new TaskAnswerStudentAppraiserEntity()
                .setId(UUID.randomUUID())
                .setStudent(studentsByName.get(appraiserName))
                .setTaskAnswerEntity(answersByStudentName.get(appraisedName))
                .setScore(0f));
    }

    private void createAnyAppraisingTask(LocalDateTime deadline,
                                         LocalDateTime appraiserDeadline,
                                         int appraisingLimit,
                                         List<String> studentNames) {
        course = new CourseEntity()
                .setId(UUID.randomUUID())
                .setName("Test Course")
                .setDescription("Test")
                .setJoinCode("TEST1234")
                .setIsArchived(false)
                .setCreatedAt(LocalDateTime.now());

        studentNames.forEach(this::addStudent);
        course.setCourseUsers(studentsByName.values().stream()
                .map(student -> new UserCourseEntity()
                        .setId(UUID.randomUUID())
                        .setCourseEntity(course)
                        .setUserEntity(student)
                        .setUserRole(UserCourseRole.STUDENT)
                        .setCreatedAt(LocalDateTime.now()))
                .toList());

        post = new PostEntity()
                .setId(UUID.randomUUID())
                .setText("Peer evaluation task")
                .setCourseEntity(course)
                .setPostType(PostType.TASK)
                .setTaskMarkEvaluationType(TaskMarkEvaluationType.SUM)
                .setTaskAnswerAppraisingType(TaskAnswerAppraisingType.ANY)
                .setStudentAppraisingNumber(appraisingLimit)
                .setMinScore(0f)
                .setMaxScore(10f)
                .setDeadline(deadline)
                .setAppraiserDeadline(appraiserDeadline)
                .setCanSeeAppraised(true)
                .setFileEntities(List.of())
                .setComments(List.of())
                .setCreatedAt(LocalDateTime.now());

        for (var entry : studentsByName.entrySet()) {
            answersByStudentName.put(entry.getKey(), new TaskAnswerEntity()
                    .setId(UUID.randomUUID())
                    .setPostEntity(post)
                    .setUserEntity(entry.getValue())
                    .setFileEntities(List.of())
                    .setComments(List.of()));
        }

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(jpaTaskAnswerRepository.findAllByPostEntityId(post.getId()))
                .thenReturn(answersByStudentName.values().stream().toList());
        when(jpaAppraiserRepository.findAllByStudentIdAndTaskAnswerEntity_PostEntityId(Mockito.any(), Mockito.eq(post.getId())))
                .thenAnswer(invocation -> {
                    UUID studentId = invocation.getArgument(0);
                    return appraiserEntities.stream()
                            .filter(appraiser -> appraiser.getStudent() != null)
                            .filter(appraiser -> appraiser.getStudent().getId().equals(studentId))
                            .toList();
                });
        when(jpaAppraiserRepository.findByTaskAnswerEntity_PostEntityId(post.getId()))
                .thenAnswer(invocation -> appraiserEntities);
        studentsByName.values().forEach(student ->
                when(userRepository.findById(student.getId())).thenReturn(Optional.of(student)));
    }

    @Given("student {string} and {string} submitted their answers")
    public void studentsSubmittedTheirAnswers(String student1, String student2) {
        answersByStudentName.get(student1).setSubmittedAt(LocalDateTime.now().minusHours(1));
        answersByStudentName.get(student2).setSubmittedAt(LocalDateTime.now().minusHours(1));
    }

    @Given("student {string} has not submitted their answer")
    public void studentHasNotSubmittedTheirAnswer(String studentName) {
        answersByStudentName.get(studentName).setSubmittedAt(null);
    }

    @When("student {string} requests available peer evaluation works")
    public void studentRequestsAvailablePeerEvaluationWorks(String studentName) {
        availableWorks = taskAnswerGeneralService.getAvailableWorksToAppraise(
                post.getId(),
                studentsByName.get(studentName).getId());
    }

    @Then("student {string} answer is available to appraise")
    public void studentAnswerIsAvailableToAppraise(String studentName) {
        var model = findModelForStudent(studentName);
        assertTrue(model.getCanAppraise());
        assertNull(model.getUnavailableReason());
    }

    @Then("student {string} answer is unavailable to appraise because {word}")
    public void studentAnswerIsUnavailableToAppraiseBecause(String studentName, String reason) {
        var model = findModelForStudent(studentName);
        assertEquals(false, model.getCanAppraise());
        assertEquals(PeerEvaluationUnavailableReason.valueOf(reason), model.getUnavailableReason());
    }

    private void addStudent(String lastName) {
        studentsByName.put(lastName, new UserEntity()
                .setId(UUID.randomUUID())
                .setFirstName("Student")
                .setLastName(lastName)
                .setEmail(lastName + "@test.com")
                .setBirthday(LocalDate.of(2000, 1, 1))
                .setCity("Moscow")
                .setPasswordHash("hash")
                .setCreatedAt(LocalDateTime.now()));
    }

    private AvailablePeerEvaluationModel findModelForStudent(String studentName) {
        var taskAnswerId = answersByStudentName.get(studentName).getId();
        var model = availableWorks.stream()
                .filter(work -> taskAnswerId.equals(work.getTaskAnswerId()))
                .findFirst()
                .orElse(null);
        assertNotNull(model);
        return model;
    }
}
