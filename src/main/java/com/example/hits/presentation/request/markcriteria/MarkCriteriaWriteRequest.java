package com.example.hits.presentation.request.markcriteria;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MarkCriteriaWriteRequest {

    private String name;

    private Float minScore;

    private Float maxScore;

    private Float multiplier;
}
