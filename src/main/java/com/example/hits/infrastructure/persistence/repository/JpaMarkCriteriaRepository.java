package com.example.hits.infrastructure.persistence.repository;

import com.example.hits.infrastructure.persistence.entity.MarkCriteriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaMarkCriteriaRepository extends JpaRepository<MarkCriteriaEntity, UUID> {

    List<MarkCriteriaEntity> findAllByPostEntity_IdOrderById(UUID postId);

    Optional<MarkCriteriaEntity> findByIdAndPostEntity_Id(UUID id, UUID postId);
}
