package com.example.hits.infrastructure.persistence.repository;

import com.example.hits.infrastructure.persistence.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<CourseEntity, UUID> {

    boolean existsByJoinCode(String joinCode);

    Optional<CourseEntity> findByJoinCode(String joinCode);
}
