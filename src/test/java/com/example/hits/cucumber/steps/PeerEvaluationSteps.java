package com.example.hits.cucumber.steps;

import com.example.hits.application.service.peer.PeerEvaluationService;
import com.example.hits.application.service.taskanswer.TaskAnswerGeneralService;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.domain.entity.post.TaskAnswerAppraisingType;
import com.example.hits.domain.entity.post.TaskMarkEvaluationType;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.infrastructure.persistence.entity.*;
import com.example.hits.infrastructure.persistence.repository.JpaCriteriaScoreRepository;
import com.example.hits.infrastructure.persistence.repository.JpaTaskAnswerRepository;
import com.example.hits.infrastructure.persistence.repository.JpaTaskAnswerStudentAppraiserRepository;
import com.example.hits.infrastructure.persistence.repository.UserRepository;
import com.example.hits.presentation.request.taskanswer.CriteriaScoreRequest;
import com.example.hits.presentation.request.taskanswer.TaskRateRequestModel;
import com.example.hits.presentation.dto.taskanswer.PeerEvaluationModel;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PeerEvaluationSteps {

    @Autowired
    private PeerEvaluationService peerEvaluationService;

    @Autowired
    private TaskAnswerGeneralService taskAnswerGeneralService;

    @Autowired
    private JpaTaskAnswerRepository jpaTaskAnswerRepository;

    @Autowired
    private JpaTaskAnswerStudentAppraiserRepository jpaAppraiserRepository;

    @Autowired
    private JpaCriteriaScoreRepository jpaCriteriaScoreRepository;

    @Autowired
    private UserRepository userRepository;

    private CourseEntity course;
    private PostEntity post;
    private TaskAnswerStudentAppraiserEntity appraiser;
    private List<PeerEvaluationModel> tasksToAppraise;

    @Before
    public void setupMocks() {
        Mockito.reset(jpaTaskAnswerRepository, jpaAppraiserRepository,
                jpaCriteriaScoreRepository, userRepository);
        Mockito.lenient().when(jpaTaskAnswerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Mockito.lenient().when(jpaAppraiserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Given("a course with {int} students")
    public void aCourseWithStudents(int studentCount) {
        course = new CourseEntity()
                .setId(UUID.randomUUID())
                .setName("Test Course")
                .setDescription("Test")
                .setJoinCode("TEST1234")
                .setIsArchived(false)
                .setCreatedAt(LocalDateTime.now());

        var users = new ArrayList<UserCourseEntity>();
        for (int i = 0; i < studentCount; i++) {
            var student = new UserEntity()
                    .setId(UUID.randomUUID())
                    .setFirstName("Student")
                    .setLastName("No" + (i + 1))
                    .setEmail("s" + (i + 1) + "@test.com")
                    .setBirthday(LocalDate.of(2000, 1, 1))
                    .setCity("Moscow")
                    .setPasswordHash("hash")
                    .setCreatedAt(LocalDateTime.now());

            users.add(new UserCourseEntity()
                    .setId(UUID.randomUUID())
                    .setCourseEntity(course)
                    .setUserEntity(student)
                    .setUserRole(UserCourseRole.STUDENT)
                    .setCreatedAt(LocalDateTime.now()));
        }
        course.setCourseUsers(users);
    }

    @Given("a task with CHAIN appraising type")
    public void aTaskWithChainAppraisingType() {
        teacherCreatesTaskWithChainAppraising();
    }

    @When("teacher creates a task with CHAIN appraising type")
    public void teacherCreatesTaskWithChainAppraising() {
        post = new PostEntity()
                .setId(UUID.randomUUID())
                .setText("Peer review task")
                .setCourseEntity(course)
                .setPostType(PostType.TASK)
                .setTaskMarkEvaluationType(TaskMarkEvaluationType.SUM)
                .setTaskAnswerAppraisingType(TaskAnswerAppraisingType.CHAIN)
                .setMinScore(0f)
                .setMaxScore(10f)
                .setFileEntities(List.of())
                .setComments(List.of())
                .setMarkCriteriaEntityList(List.of())
                .setCreatedAt(LocalDateTime.now());

        var taskAnswers = course.getCourseUsers().stream()
                .map(uc -> new TaskAnswerEntity()
                        .setId(UUID.randomUUID())
                        .setPostEntity(post)
                        .setFileEntities(List.of())
                        .setComments(List.of())
                        .setUserEntity(uc.getUserEntity()))
                .toList();
        when(jpaTaskAnswerRepository.findAllByPostEntityId(post.getId())).thenReturn(taskAnswers);

        peerEvaluationService.generateChain(post, course);
    }

    @SuppressWarnings({"unchecked"})
    @Then("{int} appraiser records are saved")
    public void appraiserRecordsAreSaved(int expectedCount) {
        var captor = ArgumentCaptor.forClass(List.class);
        verify(jpaAppraiserRepository).saveAll(captor.capture());
        assertEquals(expectedCount, captor.getValue().size());
    }

    @SuppressWarnings("unchecked")
    @Then("no student evaluates themselves")
    public void noStudentEvaluatesThemselves() {
        var captor = ArgumentCaptor.forClass(List.class);
        verify(jpaAppraiserRepository).saveAll(captor.capture());
        var appraisers = (List<TaskAnswerStudentAppraiserEntity>) captor.getValue();

        for (var a : appraisers) {
            var evaluator = a.getStudent().getId();
            var evaluated = a.getTaskAnswerEntity().getUserEntity().getId();
            assertNotEquals(evaluator, evaluated, "student should not evaluate themselves");

            for (var b : appraisers) {
                if (a == b) continue;
                var bEvaluated = b.getTaskAnswerEntity().getUserEntity().getId();
                if (evaluator.equals(bEvaluated)) {
                    assertNotEquals(evaluated, b.getStudent().getId(),
                            "two students should not evaluate each other: " + evaluator + " <-> " + evaluated);
                }
            }
        }
    }

    @Given("a task with CHAIN appraising and criteria {string} range {double}-{double} and {string} range {double}-{double}")
    public void taskWithChainAndCriteria(String name1, double min1, double max1, String name2, double min2, double max2) {
        teacherCreatesTaskWithChainAppraising();

        var criteria1 = new MarkCriteriaEntity()
                .setId(UUID.randomUUID())
                .setName(name1)
                .setPostEntity(post)
                .setMinScore((float) min1)
                .setMaxScore((float) max1);
        var criteria2 = new MarkCriteriaEntity()
                .setId(UUID.randomUUID())
                .setName(name2)
                .setPostEntity(post)
                .setMinScore((float) min2)
                .setMaxScore((float) max2);
        post.setMarkCriteriaEntityList(List.of(criteria1, criteria2));
    }

    @Given("an appraiser assigned to student {string} task answer")
    public void appraiserAssignedToStudent(String studentName) {
        var taskAnswer = jpaTaskAnswerRepository.findAllByPostEntityId(post.getId()).stream()
                .filter(ta -> ta.getUserEntity().getLastName().equals(studentName))
                .findFirst()
                .orElseThrow();

        appraiser = new TaskAnswerStudentAppraiserEntity()
                .setId(UUID.randomUUID())
                .setStudent(course.getCourseUsers().get(0).getUserEntity())
                .setTaskAnswerEntity(taskAnswer)
                .setScore(0f);

        when(jpaAppraiserRepository.findById(appraiser.getId())).thenReturn(Optional.of(appraiser));
        when(jpaAppraiserRepository.findAllByStudentIdAndTaskAnswerEntity_PostEntityId(
                appraiser.getStudent().getId(), post.getId()))
                .thenReturn(List.of(appraiser));
    }

    @Given("task hides appraised student")
    public void taskHidesAppraisedStudent() {
        post.setCanSeeAppraised(false);
    }

    @When("appraiser submits scores: {string}={int}, {string}={int}")
    public void appraiserSubmitsScores(String crit1, int score1, String crit2, int score2) {
        var markCriteria1 = post.getMarkCriteriaEntityList().stream()
                .filter(mc -> mc.getName().equals(crit1)).findFirst().orElseThrow();
        var markCriteria2 = post.getMarkCriteriaEntityList().stream()
                .filter(mc -> mc.getName().equals(crit2)).findFirst().orElseThrow();

        var entity1 = new CriteriaScoreEntity()
                .setId(UUID.randomUUID())
                .setMarkCriteriaEntity(markCriteria1)
                .setScore((float) score1)
                .setTaskAnswerStudentAppraiserEntity(appraiser);
        var entity2 = new CriteriaScoreEntity()
                .setId(UUID.randomUUID())
                .setMarkCriteriaEntity(markCriteria2)
                .setScore((float) score2)
                .setTaskAnswerStudentAppraiserEntity(appraiser);

        when(jpaCriteriaScoreRepository
                .findByTaskAnswerStudentAppraiserEntity_IdAndMarkCriteriaEntity_Id(eq(appraiser.getId()), any()))
                .thenReturn(Optional.empty());

        appraiser.setCriteriaScores(List.of(entity1, entity2));

        var requests = List.of(
                new CriteriaScoreRequest().setMarkCriteriaId(markCriteria1.getId()).setScore((float) score1),
                new CriteriaScoreRequest().setMarkCriteriaId(markCriteria2.getId()).setScore((float) score2));

        peerEvaluationService.submitAppraiserScore(appraiser.getId(), requests, appraiser.getStudent().getId());
    }

    @Then("{int} criteria score records are saved with values {double} and {double}")
    public void criteriaRecordsSaved(int count, double val1, double val2) {
        var captor = ArgumentCaptor.forClass(CriteriaScoreEntity.class);
        verify(jpaCriteriaScoreRepository, Mockito.times(count)).saveAndFlush(captor.capture());
        var scores = captor.getAllValues().stream().map(CriteriaScoreEntity::getScore).toList();
        assertEquals(count, scores.size());
    }

    @Then("appraiser score is calculated as {double}")
    public void appraiserScoreCalculatedAs(double expectedScore) {
        assertEquals((float) expectedScore, appraiser.getScore(), 0.01f);
    }

    @Given("an appraiser assigned to student {string} task answer with submitted scores {int} and {int}")
    public void appraiserAssignedWithScores(String studentName, int score1, int score2) {
        appraiserAssignedToStudent(studentName);

        var criteria1 = post.getMarkCriteriaEntityList().get(0);
        var criteria2 = post.getMarkCriteriaEntityList().get(1);

        var entity1 = new CriteriaScoreEntity()
                .setId(UUID.randomUUID())
                .setMarkCriteriaEntity(criteria1)
                .setScore((float) score1)
                .setTaskAnswerStudentAppraiserEntity(appraiser);
        var entity2 = new CriteriaScoreEntity()
                .setId(UUID.randomUUID())
                .setMarkCriteriaEntity(criteria2)
                .setScore((float) score2)
                .setTaskAnswerStudentAppraiserEntity(appraiser);

        appraiser.setCriteriaScores(List.of(entity1, entity2));
        appraiser.setScore((float) (score1 + score2));

        var taskAnswer = appraiser.getTaskAnswerEntity();
        taskAnswer.setStudentAppraiserEntities(List.of(appraiser));
        taskAnswer.setUserEntity(course.getCourseUsers().stream()
                .filter(uc -> uc.getUserEntity().getLastName().equals(studentName))
                .findFirst().orElseThrow().getUserEntity());
    }

    @When("the appraiser evaluate task answer")
    public void appraiserFinalizesEvaluation() {
        peerEvaluationService.evaluateAppraiser(
                appraiser.getId(), new TaskRateRequestModel().setRate(3F), appraiser.getStudent().getId());
    }

    @Then("the appraiser score is set")
    public void appraiserScoreIsSet() {
        var captor = ArgumentCaptor.forClass(TaskAnswerStudentAppraiserEntity.class);
        verify(jpaAppraiserRepository).save(captor.capture());
        var result = captor.getValue();
        assertEquals(3F, result.getScore());
    }

    @Then("the appraiser submittedAt is not set")
    public void appraiserSubmittedAtIsNotSet() {
        var captor = ArgumentCaptor.forClass(TaskAnswerStudentAppraiserEntity.class);
        verify(jpaAppraiserRepository).save(captor.capture());
        var saved = captor.getValue();
        assertNull(saved.getSubmittedAt(), "submittedAt should remain null after only submitting criteria");
    }

    @Then("the appraiser submittedAt is set")
    public void appraiserSubmittedAtIsSet() {
        var captor = ArgumentCaptor.forClass(TaskAnswerStudentAppraiserEntity.class);
        verify(jpaAppraiserRepository).save(captor.capture());
        var saved = captor.getValue();
        assertNotNull(saved.getSubmittedAt(), "submittedAt should be set after finalizing evaluation");
    }

    @Then("the task answer score is recalculated")
    public void taskAnswerScoreIsRecalculated() {
        var captor = ArgumentCaptor.forClass(TaskAnswerEntity.class);
        verify(jpaTaskAnswerRepository).save(captor.capture());
        var savedAnswer = captor.getValue();
        assertEquals(appraiser.getScore(), savedAnswer.getScore(), 0.01f,
                "task answer score should equal the only submitted appraiser score");
    }

    @When("teacher overrides the appraiser score to {double}")
    public void teacherOverridesAppraiserScore(double newScore) {
        var teacherId = UUID.randomUUID();
        var teacherUser = new UserEntity()
                .setId(teacherId)
                .setFirstName("Teacher")
                .setLastName("One")
                .setEmail("teacher@test.com")
                .setBirthday(LocalDate.of(1980, 1, 1))
                .setCity("Moscow")
                .setPasswordHash("hash")
                .setCreatedAt(LocalDateTime.now());

        course.getCourseUsers().add(new UserCourseEntity()
                .setId(UUID.randomUUID())
                .setCourseEntity(course)
                .setUserEntity(teacherUser)
                .setUserRole(UserCourseRole.TEACHER)
                .setCreatedAt(LocalDateTime.now()));

        when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacherUser));

        peerEvaluationService.overrideAppraiserScore(
                appraiser.getId(), (float) newScore, teacherId);
    }

    @Then("appraiser score is {double}")
    public void appraiserScoreIs(double expectedScore) {
        assertEquals((float) expectedScore, appraiser.getScore(), 0.01f);
    }

    @When("appraiser requests tasks to appraise")
    public void appraiserRequestsTasksToAppraise() {
        tasksToAppraise = taskAnswerGeneralService.getTasksToAppraise(appraiser.getStudent().getId(), post.getId());
    }

    @Then("appraised student is hidden in tasks to appraise")
    public void appraisedStudentIsHiddenInTasksToAppraise() {
        assertEquals(1, tasksToAppraise.size());
        assertNull(tasksToAppraise.getFirst().getStudent());
    }
}
