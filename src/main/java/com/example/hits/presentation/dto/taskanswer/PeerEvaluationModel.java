package com.example.hits.presentation.dto.taskanswer;

import com.example.hits.presentation.dto.file.FileModel;
import com.example.hits.presentation.dto.markcriteria.ScoredMarkCriteriaModel;
import com.example.hits.presentation.dto.user.UserModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class PeerEvaluationModel {

    private UUID id;

    private UserModel student;

    private UserModel appraiser;

    private Float score;

    private LocalDateTime submittedAt;

    private UUID taskAnswerId;

    private List<ScoredMarkCriteriaModel> criteriaScores;

    private List<FileModel> files;
}
