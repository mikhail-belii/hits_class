package com.example.hits.cucumber.steps;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.mapper.UserMapper;
import com.example.hits.application.service.course.CourseCodeGenerator;
import com.example.hits.application.service.course.CourseService;
import com.example.hits.application.service.peer.PeerEvaluationAvailabilityService;
import com.example.hits.application.service.peer.PeerEvaluationService;
import com.example.hits.application.service.taskanswer.TaskAnswerGeneralService;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.repository.JpaCourseRepository;
import com.example.hits.infrastructure.persistence.entity.*;
import com.example.hits.infrastructure.persistence.repository.*;
import com.example.hits.presentation.dto.course.AppraiserTopCourseModel;
import com.example.hits.presentation.dto.taskanswer.TaskAnswerModel;
import com.example.hits.presentation.dto.user.UserModel;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class CourseAppraisersTopSteps {

    private CourseService courseService;
    private UserRepository userRepository;
    private JpaCourseRepository jpaCourseRepository;
    private JpaTaskAnswerRepository jpaTaskAnswerRepository;
    private UserCourseRepository userCourseRepository;
    private CourseCodeGenerator courseCodeGenerator;
    private TaskAnswerGeneralService taskAnswerGeneralService;
    private PeerEvaluationService peerEvaluationService;
    private CourseRepository courseRepository;
    private UserMapper userMapper;

    private CourseEntity courseEntity;
    private UserEntity userEntity;
    private UserCourseEntity userCourseEntity;
    private TaskAnswerEntity taskAnswerEntity;

    private List<AppraiserTopCourseModel> result;
    private ExceptionWrapper errorResult;

    @Before
    public void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        jpaCourseRepository = Mockito.mock(JpaCourseRepository.class);
        jpaTaskAnswerRepository = Mockito.mock(JpaTaskAnswerRepository.class);
        userCourseRepository = Mockito.mock(UserCourseRepository.class);
        courseCodeGenerator = Mockito.mock(CourseCodeGenerator.class);
        taskAnswerGeneralService = Mockito.mock(TaskAnswerGeneralService.class);
        peerEvaluationService = Mockito.mock(PeerEvaluationService.class);
        courseRepository = Mockito.mock(CourseRepository.class);
        userMapper = Mockito.mock(UserMapper.class);

        courseService = new CourseService(
                userRepository,
                jpaCourseRepository,
                jpaTaskAnswerRepository,
                userCourseRepository,
                courseCodeGenerator,
                taskAnswerGeneralService,
                peerEvaluationService,
                courseRepository,
                userMapper);
    }

    @Given("a course that has not a user in it")
    public void aCourseThatHasNotAUserInIt() {
        courseEntity = new CourseEntity()
                .setId(UUID.randomUUID());
        courseEntity.setCourseUsers(List.of());
        userEntity = new UserEntity()
                .setId(UUID.randomUUID());
        userCourseEntity = new UserCourseEntity()
                .setCourseEntity(courseEntity)
                .setUserEntity(userEntity);
    }

    @When("user requests a course appraiser top with error")
    public void userRequestsACourseAppraiserTopWithError() {
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));
        when(jpaCourseRepository.findById(courseEntity.getId())).thenReturn(Optional.of(courseEntity));

        errorResult = assertThrows(ExceptionWrapper.class, () ->
                courseService.getCourseAppraisersTop(userEntity.getId(), courseEntity.getId()));
    }

    @Then("throws forbidden rights exception")
    public void throwsForbiddenRightsException() {
        assertEquals("User has no rights to this action", errorResult.getErrors().get("forbidden"));
    }

    @Given("a course with one not evaluated appraiser")
    public void aCourseWithOneNotEvaluatedAppraiser() {
        courseEntity = new CourseEntity()
                .setId(UUID.randomUUID());
        userEntity = new UserEntity()
                .setId(UUID.randomUUID());
        userCourseEntity = new UserCourseEntity()
                .setCourseEntity(courseEntity)
                .setUserEntity(userEntity)
                .setUserRole(UserCourseRole.STUDENT);
        courseEntity.setCourseUsers(List.of(userCourseEntity));
        TaskAnswerStudentAppraiserEntity taskAnswerStudentAppraiserEntity = new TaskAnswerStudentAppraiserEntity()
                .setScore(null);
        taskAnswerEntity = new TaskAnswerEntity()
                .setStudentAppraiserEntities(List.of(taskAnswerStudentAppraiserEntity));
    }

    @When("user requests a course appraiser top")
    public void userRequestsACourseAppraiserTop() {
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));
        when(jpaCourseRepository.findById(courseEntity.getId())).thenReturn(Optional.of(courseEntity));
        when(jpaTaskAnswerRepository.findAllByPostEntityCourseEntityId(courseEntity.getId())).thenReturn(List.of(taskAnswerEntity));
        when(userMapper.toModel(userEntity)).thenReturn(new UserModel().setId(userEntity.getId()));

        result = courseService.getCourseAppraisersTop(userEntity.getId(), courseEntity.getId());
    }

    @Then("returns appraiser top with one user with 0 match percentage and appraised number")
    public void returnsAppraiserTopWithOneUserWith0MatchPercentageAppraisedNumber() {
        assertEquals(1, result.size());
        var appraiserTopElement = result.get(0);
        assertEquals(0, appraiserTopElement.getMatchPercentage());
        assertEquals(0, appraiserTopElement.getAppraisedNumber());
        assertEquals(userEntity.getId(), appraiserTopElement.getStudentModel().getId());
    }

    @Given("a course with one evaluated appraiser with minScore = {float}, maxScore = {float}, appraiserScore = {float}, teacherScore = {float}")
    public void aCourseWithOneEvaluatedAppraiser(Float minScore, Float maxScore, Float appraiserScore, Float teacherScore) {
        courseEntity = new CourseEntity()
                .setId(UUID.randomUUID());
        userEntity = new UserEntity()
                .setId(UUID.randomUUID());
        userCourseEntity = new UserCourseEntity()
                .setCourseEntity(courseEntity)
                .setUserEntity(userEntity)
                .setUserRole(UserCourseRole.STUDENT);
        courseEntity.setCourseUsers(List.of(userCourseEntity));
        TaskAnswerStudentAppraiserEntity taskAnswerStudentAppraiserEntity = new TaskAnswerStudentAppraiserEntity()
                .setScore(appraiserScore)
                .setStudent(userEntity);
        PostEntity postEntity = new PostEntity()
                .setMaxScore(maxScore)
                .setMinScore(minScore);
        taskAnswerEntity = new TaskAnswerEntity()
                .setStudentAppraiserEntities(List.of(taskAnswerStudentAppraiserEntity))
                .setPostEntity(postEntity)
                .setScore(appraiserScore)
                .setTeacherScore(teacherScore);
    }

    @Given("a course with one evaluated appraiser with minScore = {float}, maxScore = {float}, appraiserScore = {float}, teacherScore = null")
    public void aCourseWithOneEvaluatedAppraiserAndTeacherScoreNull(Float minScore, Float maxScore, Float appraiserScore) {
        courseEntity = new CourseEntity()
                .setId(UUID.randomUUID());
        userEntity = new UserEntity()
                .setId(UUID.randomUUID());
        userCourseEntity = new UserCourseEntity()
                .setCourseEntity(courseEntity)
                .setUserEntity(userEntity)
                .setUserRole(UserCourseRole.STUDENT);
        courseEntity.setCourseUsers(List.of(userCourseEntity));
        TaskAnswerStudentAppraiserEntity taskAnswerStudentAppraiserEntity = new TaskAnswerStudentAppraiserEntity()
                .setScore(appraiserScore)
                .setStudent(userEntity);
        PostEntity postEntity = new PostEntity()
                .setMaxScore(maxScore)
                .setMinScore(minScore);
        taskAnswerEntity = new TaskAnswerEntity()
                .setStudentAppraiserEntities(List.of(taskAnswerStudentAppraiserEntity))
                .setPostEntity(postEntity)
                .setScore(appraiserScore)
                .setTeacherScore(null);
    }

    @Then("returns appraiser top with one user with {int} matchPercentage and one appraised number")
    public void returnsAppraiserTopWithOneUserWithSelectedMatchPercentageAndOneAppraiserNumber(Integer matchPercentage) {
        assertEquals(1, result.size());
        var appraiserTopElement = result.get(0);
        assertEquals(matchPercentage, appraiserTopElement.getMatchPercentage());
        assertEquals(1, appraiserTopElement.getAppraisedNumber());
        assertEquals(userEntity.getId(), appraiserTopElement.getStudentModel().getId());
    }

}
