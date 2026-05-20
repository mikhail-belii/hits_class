package com.example.hits.application.mapper;

import com.example.hits.domain.aggregate.ScoredMarkCriteria;
import com.example.hits.domain.entity.markCriteria.MarkCriteria;
import com.example.hits.infrastructure.persistence.entity.CriteriaScoreEntity;
import com.example.hits.infrastructure.persistence.entity.MarkCriteriaEntity;
import com.example.hits.presentation.dto.markcriteria.MarkCriteriaModel;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CriteriaMapper {

    public ScoredMarkCriteria toDomain(@NotNull MarkCriteriaEntity markCriteriaEntity, CriteriaScoreEntity criteriaScoreEntity) {
        return new ScoredMarkCriteria(
                markCriteriaEntity.getEvaluationFunction(),
                markCriteriaEntity.getName(),
                markCriteriaEntity.getMultiplier(),
                markCriteriaEntity.getMinScore(),
                criteriaScoreEntity != null ? criteriaScoreEntity.getScore(): markCriteriaEntity.getMinScore(),
                markCriteriaEntity.getMaxScore(),
                markCriteriaEntity.getPostEntity().getId());
    }

    public static MarkCriteriaModel toModel(MarkCriteria domain) {
        if (domain == null) {
            return null;
        }
        return new MarkCriteriaModel()
                .setId(domain.getId())
                .setEvaluationFunction(domain.getEvaluationFunction())
                .setName(domain.getName())
                .setDescription(domain.getDescription())
                .setMultiplier(domain.getMultiplier())
                .setMinScore(domain.getMinScore())
                .setMaxScore(domain.getMaxScore())
                .setPostId(domain.getPostId());
    }
}
