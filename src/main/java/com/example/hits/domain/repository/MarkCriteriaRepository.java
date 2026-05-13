package com.example.hits.domain.repository;

import com.example.hits.domain.entity.markCriteria.MarkCriteria;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarkCriteriaRepository {

    List<MarkCriteria> findAllByPostId(UUID postId);

    Optional<MarkCriteria> findByIdAndPostId(UUID markCriteriaId, UUID postId);

    void save(MarkCriteria markCriteria);

    boolean deleteWithScores(UUID markCriteriaId, UUID postId);
}
