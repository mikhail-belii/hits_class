package com.example.hits.presentation.request.post;

import com.example.hits.domain.entity.markCriteria.EvaluationFunction;
import com.example.hits.domain.entity.post.TaskMarkEvaluationType;

public interface TaskCreationFields {

    TaskMarkEvaluationType getTaskMarkEvaluationType();

    Float getMaxScore();

    Float getMinScore();

    Float getMultiplier();

    Float getPassThreshold();

    EvaluationFunction getEvaluationFunction();

}
