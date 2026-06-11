package com.example.hits.domain.entity.post;

import com.example.hits.domain.entity.markCriteria.EvaluationFunction;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class Post {

    private UUID id;

    private String text;

    private LocalDateTime updatedAt;

    private UUID courseId;

    private UUID authorId;

    private List<UUID> fileEntityIds;

    private List<UUID> commentIds;

    private PostType postType;

    private TaskMarkEvaluationType taskMarkEvaluationType;

    private LocalDateTime createdAt;

    private LocalDateTime deadline;

    private Float multiplier;

    private EvaluationFunction evaluationFunction;

    private Float minScore;

    private Float maxScore;

    private Integer studentAppraisingNumber;

    private Float passThreshold;

    private LocalDateTime appraiserDeadline;

    private TaskAnswerAppraisingType taskAnswerAppraisingType;

    private Boolean canSeeAppraiser;

    private Boolean canSeeAppraised;

}
