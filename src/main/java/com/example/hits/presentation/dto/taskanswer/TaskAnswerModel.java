package com.example.hits.presentation.dto.taskanswer;

import com.example.hits.domain.entity.taskanswer.TaskAnswerEvaluationStatus;
import com.example.hits.presentation.dto.comment.taskanswercomment.TaskAnswerCommentModel;
import com.example.hits.presentation.dto.file.FileModel;
import com.example.hits.domain.entity.taskanswer.TaskAnswerStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TaskAnswerModel {

    private UUID id = UUID.randomUUID();

    private Float score = null;

    private Boolean isScoredByTeacher;

    private Float minScore;

    private Float maxScore = 100f;

    private LocalDateTime submittedAt = null;

    private TaskAnswerStatus status = TaskAnswerStatus.NOT_COMPLETED;

    private TaskAnswerEvaluationStatus evaluationStatus
            = TaskAnswerEvaluationStatus.NOT_EVALUATED;

    private List<FileModel> files = new ArrayList<>();

    private List<TaskAnswerCommentModel> comments = new ArrayList<>();

    private String postName;

    private UUID postId;

    private UUID courseId;
}
