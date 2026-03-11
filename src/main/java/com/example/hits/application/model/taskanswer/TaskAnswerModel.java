package com.example.hits.application.model.taskanswer;

import com.example.hits.application.model.attachment.AttachmentModel;
import com.example.hits.application.model.comment.taskanswercomment.TaskAnswerCommentModel;
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

    private Integer score = null;

    private Integer maxScore = 100;

    private LocalDateTime submittedAt = null;

    private TaskAnswerStatus status = TaskAnswerStatus.NOT_COMPLETED;

    private List<AttachmentModel> attachments = new ArrayList<>();

    private List<TaskAnswerCommentModel> comments = new ArrayList<>();

    private String postName;
}
