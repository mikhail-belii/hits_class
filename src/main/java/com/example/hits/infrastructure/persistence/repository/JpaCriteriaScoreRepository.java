package com.example.hits.infrastructure.persistence.repository;

import com.example.hits.infrastructure.persistence.entity.CriteriaScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface JpaCriteriaScoreRepository extends JpaRepository<CriteriaScoreEntity, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM CriteriaScoreEntity c WHERE c.markCriteriaEntity.id = :markCriteriaId")
    void deleteAllByMarkCriteriaId(@Param("markCriteriaId") UUID markCriteriaId);
}
