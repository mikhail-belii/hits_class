package com.example.hits.application.repository;

import com.example.hits.domain.entity.course.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {

    boolean existsByJoinCode(String joinCode);

    Optional<Course> findByJoinCode(String joinCode);
}
