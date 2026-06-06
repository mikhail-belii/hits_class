package com.example.hits.cucumber;

import com.example.hits.HitsClass;
import com.example.hits.infrastructure.persistence.repository.CourseRepository;
import com.example.hits.infrastructure.persistence.repository.JpaCriteriaScoreRepository;
import com.example.hits.infrastructure.persistence.repository.JpaTaskAnswerRepository;
import com.example.hits.infrastructure.persistence.repository.JpaTaskAnswerStudentAppraiserRepository;
import com.example.hits.infrastructure.persistence.repository.UserCourseRepository;
import com.example.hits.infrastructure.persistence.repository.UserRepository;
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
