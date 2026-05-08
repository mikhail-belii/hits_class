package com.example.hits.infrastructure.persistence.repository;

import com.example.hits.infrastructure.persistence.entity.MarkCriteriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaMarkCriteriaRepository extends JpaRepository<MarkCriteriaEntity, UUID> {

}
