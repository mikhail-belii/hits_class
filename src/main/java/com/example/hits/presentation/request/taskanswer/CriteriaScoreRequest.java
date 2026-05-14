package com.example.hits.presentation.request.taskanswer;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class CriteriaScoreRequest {

    private UUID markCriteriaId;

    private Float score;
}
