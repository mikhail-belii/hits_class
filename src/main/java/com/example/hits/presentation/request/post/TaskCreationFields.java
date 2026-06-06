package com.example.hits.presentation.request.post;

import com.example.hits.domain.entity.markCriteria.EvaluationFunction;
import com.example.hits.domain.entity.post.TaskAnswerAppraisingType;
import com.example.hits.domain.entity.post.TaskMarkEvaluationType;

import java.time.LocalDateTime;

public interface TaskCreationFields {

    TaskMarkEvaluationType getTaskMarkEvaluationType();

    Float getMaxScore();

    Float getMinScore();

    Float getMultiplier();

    Float getPassThreshold();

    EvaluationFunction getEvaluationFunction();

    LocalDateTime getAppraiserDeadline();

    TaskAnswerAppraisingType getTaskAnswerAppraisingType();

}
