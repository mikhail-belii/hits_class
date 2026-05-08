package com.example.hits.presentation.dto.taskanswer;

import com.example.hits.domain.entity.post.TaskMarkEvaluationType;
import com.example.hits.presentation.dto.markcriteria.ScoredMarkCriteriaModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TaskAnswerCriteriaScoresModel {

    private UUID taskAnswerId;

    private TaskMarkEvaluationType taskMarkEvaluationType;

    private List<ScoredMarkCriteriaModel> scoredMarkCriteriaModels;

}
