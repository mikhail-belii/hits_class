package com.example.hits.infrastructure.persistence.repository;

import com.example.hits.infrastructure.persistence.entity.CriteriaScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaCriteriaScoreRepository extends JpaRepository<CriteriaScoreEntity, UUID> {

}
