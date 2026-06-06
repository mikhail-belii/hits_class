package com.example.hits.presentation.request.post;

import com.example.hits.domain.entity.markCriteria.EvaluationFunction;
import com.example.hits.domain.entity.post.TaskAnswerAppraisingType;
import com.example.hits.domain.entity.post.TaskMarkEvaluationType;
import com.example.hits.presentation.dto.file.FileModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Accessors(chain=true)
public class PostUpdateModel implements TaskCreationFields {
    private String text;

    private List<FileModel> files;

    private TaskMarkEvaluationType taskMarkEvaluationType;

    private Float maxScore;

    private Float minScore;

    private Float multiplier;

    private Float passThreshold;

    private EvaluationFunction evaluationFunction;

    private LocalDateTime appraiserDeadline;

    private TaskAnswerAppraisingType taskAnswerAppraisingType;

    private Boolean canSeeAppraiser;

    private Boolean canSeeAppraised;
}
