package com.example.hits.presentation.dto.markcriteria;

import com.example.hits.domain.entity.markCriteria.EvaluationFunction;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.presentation.dto.comment.postcomment.PostCommentModel;
import com.example.hits.presentation.dto.file.FileModel;
import com.example.hits.presentation.dto.taskanswer.TaskAnswerModel;
import com.example.hits.presentation.dto.user.UserModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain=true)
public class MarkCriteriaModel {

    private UUID id;

    private EvaluationFunction evaluationFunction;

    private String name;

    private Float multiplier;

    private Float minScore;

    private Float maxScore;

    private Float passThreshold;

    private UUID postId;
}
