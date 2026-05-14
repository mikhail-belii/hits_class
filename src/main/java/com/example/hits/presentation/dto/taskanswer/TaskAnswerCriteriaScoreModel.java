package com.example.hits.presentation.dto.taskanswer;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class TaskAnswerCriteriaScoreModel {

    private UUID markCriteriaId;

    private String name;

    private Float score;
}
