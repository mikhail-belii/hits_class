package com.example.hits.domain.entity.markCriteria;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class CriteriaScore {

    private UUID id;

    private Float score;

    private UUID markCriteriaId;

    private UUID taskAnswerId;

}
