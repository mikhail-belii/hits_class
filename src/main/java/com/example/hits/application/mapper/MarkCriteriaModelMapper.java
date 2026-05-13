package com.example.hits.application.mapper;

import com.example.hits.domain.entity.markCriteria.MarkCriteria;
import com.example.hits.presentation.dto.markcriteria.MarkCriteriaModel;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MarkCriteriaModelMapper {

    public static MarkCriteriaModel toModel(MarkCriteria domain) {
        if (domain == null) {
            return null;
        }
        return new MarkCriteriaModel()
                .setId(domain.getId())
                .setEvaluationFunction(domain.getEvaluationFunction())
                .setName(domain.getName())
                .setMultiplier(domain.getMultiplier())
                .setMinScore(domain.getMinScore())
                .setMaxScore(domain.getMaxScore())
                .setPostId(domain.getPostId());
    }
}
