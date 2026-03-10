package com.example.hits.domain.mapper;

import com.example.hits.application.model.attachment.AttachmentModel;
import com.example.hits.application.model.taskanswer.TaskAnswerModel;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.taskanswer.TaskAnswerStatus;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
@ExtensionMethod(TaskAnswerCommentMapper.class)
public class TaskAnswerMapper {

    public TaskAnswerModel toModel(TaskAnswer taskAnswer) {
        String postText = taskAnswer.getPost().getText();

        return new TaskAnswerModel()
                .setId(taskAnswer.getId())
                .setScore(taskAnswer.getScore())
                .setSubmittedAt(taskAnswer.getSubmittedAt())
                .setStatus(parseStatus(taskAnswer))
                .setAttachments(taskAnswer.getAttachments().stream()
                        .map(attach -> new AttachmentModel(attach.getId()))
                        .toList())
                .setComments(taskAnswer.getComments().stream()
                        .map(TaskAnswerCommentMapper::toModel)
                        .toList())
                .setPostName(postText.substring(0, Math.min(10, postText.length())));
    }

    private TaskAnswerStatus parseStatus(TaskAnswer taskAnswer) {
        LocalDateTime submittedAt = taskAnswer.getSubmittedAt();

        if (submittedAt == null) {
            LocalDateTime createdAt = taskAnswer.getPost().getCreatedAt();
            return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusDays(7))
                    ? TaskAnswerStatus.NEW
                    : TaskAnswerStatus.NOT_COMPLETED;
        }

        LocalDateTime deadline = taskAnswer.getPost().getDeadline();
        return deadline == null || !submittedAt.isAfter(deadline)
                ? TaskAnswerStatus.COMPLETED
                : TaskAnswerStatus.COMPETED_AFTER_DEADLINE;
    }
}
