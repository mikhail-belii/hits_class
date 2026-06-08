package com.example.hits.cucumber;

import com.example.hits.HitsClass;
import com.example.hits.infrastructure.persistence.repository.*;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@CucumberContextConfiguration
@SpringBootTest(classes = HitsClass.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {

    @MockBean
    private JpaTaskAnswerRepository jpaTaskAnswerRepository;

    @MockBean
    private JpaTaskAnswerStudentAppraiserRepository jpaAppraiserRepository;

    @MockBean
    private JpaCriteriaScoreRepository jpaCriteriaScoreRepository;

    @MockBean
    private UserCourseRepository userCourseRepository;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private UserRepository userRepository;
}
