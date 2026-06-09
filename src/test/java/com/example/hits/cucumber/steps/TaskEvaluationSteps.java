package com.example.hits.cucumber.steps;

import com.example.hits.application.service.peer.PeerEvaluationAvailabilityService;
import com.example.hits.application.service.taskanswer.TaskAnswerGeneralService;
import com.example.hits.infrastructure.persistence.entity.*;
import com.example.hits.infrastructure.persistence.repository.*;
import com.example.hits.presentation.dto.taskanswer.TaskAnswerModel;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskEvaluationSteps {

    private JpaTaskAnswerRepository jpaTaskAnswerRepository;
    private UserRepository userRepository;
    private PostRepository postRepository;
    private JpaTaskAnswerStudentAppraiserRepository jpaAppraiserRepository;
    private PeerEvaluationAvailabilityService peerEvaluationAvailabilityService;
    private TaskAnswerGeneralService taskAnswerGeneralService;

    private PostEntity postEntity;
    private UserEntity userEntity;
    private TaskAnswerEntity taskAnswerEntity;

    private TaskAnswerModel result;

    @Before
    public void setUp() {
        jpaTaskAnswerRepository = Mockito.mock(JpaTaskAnswerRepository.class);
        peerEvaluationAvailabilityService = Mockito.mock(PeerEvaluationAvailabilityService.class);
        taskAnswerGeneralService = new TaskAnswerGeneralService(
                jpaTaskAnswerRepository,
                userRepository,
                postRepository,
                jpaAppraiserRepository,
                peerEvaluationAvailabilityService);
    }

    @Given("a task answer scored by students but not the teachers")
    public void aTaskAnswerScoredByStudentsButNotTheTeachers() {
        postEntity = new PostEntity()
                .setId(UUID.randomUUID());
        userEntity = new UserEntity()
                .setId(UUID.randomUUID());
        taskAnswerEntity = new TaskAnswerEntity()
                .setScore(2F)
                .setPostEntity(postEntity)
                .setUserEntity(userEntity);
    }

    @When("student requests a task answer model")
    public void studentRequestsATaskAnswerModel() {
        when(jpaTaskAnswerRepository.findByUserEntityIdAndPostEntityId(any(), any()))
                .thenReturn(Optional.of(taskAnswerEntity));

        result = taskAnswerGeneralService.getUserPostTaskAnswer(postEntity.getId(), userEntity.getId());
    }

    @SuppressWarnings({"unchecked"})
    @Then("task answer model with students score and isScoredByTeacher equals false")
    public void taskAnswerModelWithStudentsScoreAndIsScoredByTeacherEqualsFalse() {
        assertEquals(taskAnswerEntity.getScore(), result.getScore());
        assertEquals(false, result.getIsScoredByTeacher());
    }

    @Given("a task answer scored by teacher but not the students")
    public void aTaskAnswerScoredByTeacherButNotTheStudents() {
        postEntity = new PostEntity()
                .setId(UUID.randomUUID());
        userEntity = new UserEntity()
                .setId(UUID.randomUUID());
        taskAnswerEntity = new TaskAnswerEntity()
                .setTeacherScore(2F)
                .setPostEntity(postEntity)
                .setUserEntity(userEntity);
    }

    @SuppressWarnings({"unchecked"})
    @Then("task answer model with teacher score and isScoredByTeacher equals true")
    public void taskAnswerModelWithTeacherScoreAndIsScoredByTeacherEqualsTrue() {
        assertEquals(taskAnswerEntity.getTeacherScore(), result.getScore());
        assertEquals(true, result.getIsScoredByTeacher());
    }

    @Given("a task answer scored by teacher and students")
    public void aTaskAnswerScoredByTeacherAndStudents() {
        postEntity = new PostEntity()
                .setId(UUID.randomUUID());
        userEntity = new UserEntity()
                .setId(UUID.randomUUID());
        taskAnswerEntity = new TaskAnswerEntity()
                .setTeacherScore(2F)
                .setScore(4F)
                .setPostEntity(postEntity)
                .setUserEntity(userEntity);
    }
}
