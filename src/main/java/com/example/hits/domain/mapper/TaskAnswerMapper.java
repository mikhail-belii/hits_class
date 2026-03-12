package com.example.hits.domain.mapper;

import com.example.hits.application.model.file.FileModel;
import com.example.hits.application.model.taskanswer.TaskAnswerFullModel;
import com.example.hits.application.model.taskanswer.TaskAnswerModel;
import com.example.hits.domain.entity.file.File;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.taskanswer.TaskAnswerStatus;
import com.example.hits.domain.entity.taskanswercomment.TaskAnswerComment;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
@ExtensionMethod({ TaskAnswerCommentMapper.class, SimpleUserMapper.class })
public class TaskAnswerMapper {

    private static final int MAX_POST_NAME_LENGTH = 10;

    public TaskAnswerModel toModel(TaskAnswer taskAnswer) {
        String postText = extractPostText(taskAnswer);
        postText = postText.length() > MAX_POST_NAME_LENGTH ? postText.substring(0, MAX_POST_NAME_LENGTH) + "..." : postText;

        return new TaskAnswerModel()
                .setId(taskAnswer.getId())
                .setScore(taskAnswer.getScore())
                .setMaxScore(taskAnswer.getPost() != null ? taskAnswer.getPost().getMaxScore() : null)
                .setSubmittedAt(taskAnswer.getSubmittedAt())
                .setStatus(parseStatus(taskAnswer))
                .setFiles(safeFiles(taskAnswer).stream()
                        .map(file -> new FileModel(file.getId(), "name"))
                        .toList())
                .setComments(safeComments(taskAnswer).stream()
                        .map(TaskAnswerCommentMapper::toModel)
                        .toList())
                .setPostName(postText)
                .setPostId(taskAnswer.getPost() != null ? taskAnswer.getPost().getId() : null)
                .setCourseId(taskAnswer.getPost() != null && taskAnswer.getPost().getCourse() != null
                        ? taskAnswer.getPost().getCourse().getId() : null);
    }

    public TaskAnswerFullModel toFullModel(TaskAnswer taskAnswer) {
        String postText = extractPostText(taskAnswer);

        return new TaskAnswerFullModel()
                .setId(taskAnswer.getId())
                .setScore(taskAnswer.getScore())
                .setMaxScore(taskAnswer.getPost() != null ? taskAnswer.getPost().getMaxScore() : null)
                .setSubmittedAt(taskAnswer.getSubmittedAt())
                .setStatus(parseStatus(taskAnswer))
                .setFiles(safeFiles(taskAnswer).stream()
                        .map(file -> new FileModel(file.getId(), "name"))
                        .toList())
                .setComments(safeComments(taskAnswer).stream()
                        .map(TaskAnswerCommentMapper::toModel)
                        .toList())
                .setPostName(postText.substring(0, Math.min(MAX_POST_NAME_LENGTH, postText.length())))
                .setPostId(taskAnswer.getPost() != null ? taskAnswer.getPost().getId() : null)
                .setUser(taskAnswer.getUser() != null ? taskAnswer.getUser().toModel() : null)
                .setCourseId(taskAnswer.getPost() != null && taskAnswer.getPost().getCourse() != null
                        ? taskAnswer.getPost().getCourse().getId() : null);
    }

    private TaskAnswerStatus parseStatus(TaskAnswer taskAnswer) {
        LocalDateTime submittedAt = taskAnswer.getSubmittedAt();

        if (submittedAt == null) {
            LocalDateTime createdAt = taskAnswer.getPost() != null ? taskAnswer.getPost().getCreatedAt() : null;
            return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusDays(7))
                    ? TaskAnswerStatus.NEW
                    : TaskAnswerStatus.NOT_COMPLETED;
        }

        LocalDateTime deadline = taskAnswer.getPost() != null ? taskAnswer.getPost().getDeadline() : null;
        return deadline == null || !submittedAt.isAfter(deadline)
                ? TaskAnswerStatus.COMPLETED
                : TaskAnswerStatus.COMPETED_AFTER_DEADLINE;
    }

    private String extractPostText(TaskAnswer taskAnswer) {
        if (taskAnswer.getPost() == null || taskAnswer.getPost().getText() == null) {
            return "";
        }

        return taskAnswer.getPost().getText();
    }

    private List<File> safeFiles(TaskAnswer taskAnswer) {
        return taskAnswer.getFiles() != null ? taskAnswer.getFiles() : List.of();
    }

    private List<TaskAnswerComment> safeComments(TaskAnswer taskAnswer) {
        return taskAnswer.getComments() != null ? taskAnswer.getComments() : List.of();
    }
}
