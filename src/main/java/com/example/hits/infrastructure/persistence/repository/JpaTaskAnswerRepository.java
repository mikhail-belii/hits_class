package com.example.hits.infrastructure.persistence.repository;

import com.example.hits.infrastructure.persistence.entity.TaskAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaTaskAnswerRepository extends JpaRepository<TaskAnswerEntity, UUID> {
    Optional<TaskAnswerEntity> findByUserEntityIdAndPostEntityId(UUID userId, UUID postId);
    List<TaskAnswerEntity> findAllByUserEntityIdAndPostEntityCourseEntityId(UUID userId, UUID courseId);
    List<TaskAnswerEntity> findAllByPostEntityId(UUID postId);
}
