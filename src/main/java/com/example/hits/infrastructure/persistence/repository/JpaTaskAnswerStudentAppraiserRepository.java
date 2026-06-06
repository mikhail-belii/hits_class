package com.example.hits.infrastructure.persistence.repository;

import com.example.hits.infrastructure.persistence.entity.TaskAnswerStudentAppraiserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaTaskAnswerStudentAppraiserRepository extends JpaRepository<TaskAnswerStudentAppraiserEntity, UUID> {

    List<TaskAnswerStudentAppraiserEntity> findByTaskAnswerEntity_PostEntityId(UUID postId);

    List<TaskAnswerStudentAppraiserEntity> findAllByStudentId(UUID studentId);

    List<TaskAnswerStudentAppraiserEntity> findAllByStudentIdAndTaskAnswerEntity_PostEntityId(UUID studentId, UUID postId);

    List<TaskAnswerStudentAppraiserEntity> findAllByTaskAnswerEntityId(UUID taskAnswerId);

}
