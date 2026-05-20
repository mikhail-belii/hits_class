package com.example.hits.infrastructure.persistence.mapper;

import com.example.hits.domain.entity.markCriteria.MarkCriteria;
import com.example.hits.infrastructure.persistence.entity.MarkCriteriaEntity;
import com.example.hits.infrastructure.persistence.entity.PostEntity;
import org.springframework.stereotype.Component;

@Component
public class MarkCriteriaPersistenceMapper {

    public MarkCriteria toDomain(MarkCriteriaEntity entity) {
        if (entity == null) {
            return null;
        }
        return MarkCriteria.restore(
                entity.getId(),
                entity.getPostEntity() != null ? entity.getPostEntity().getId() : null,
                entity.getName(),
                entity.getDescription(),
                entity.getMinScore(),
                entity.getMaxScore(),
                entity.getMultiplier(),
                entity.getEvaluationFunction()
        );
    }

    public void copyToEntity(MarkCriteria domain, MarkCriteriaEntity entity, PostEntity post) {
        entity.setId(domain.getId());
        entity.setPostEntity(post);
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setMinScore(domain.getMinScore());
        entity.setMaxScore(domain.getMaxScore());
        entity.setMultiplier(domain.getMultiplier());
        entity.setEvaluationFunction(domain.getEvaluationFunction());
    }
}
