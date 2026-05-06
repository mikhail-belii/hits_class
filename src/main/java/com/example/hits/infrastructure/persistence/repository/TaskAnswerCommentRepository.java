package com.example.hits.infrastructure.persistence.repository;

import com.example.hits.infrastructure.persistence.entity.TaskAnswerCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskAnswerCommentRepository extends JpaRepository<TaskAnswerCommentEntity, UUID> {
}
