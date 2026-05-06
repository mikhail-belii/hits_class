package com.example.hits.application.mapper;

import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.infrastructure.persistence.entity.FileEntity;
import com.example.hits.presentation.dto.file.FileModel;
import com.example.hits.presentation.dto.taskanswer.TaskAnswerFullModel;
import com.example.hits.presentation.dto.taskanswer.TaskAnswerModel;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerEntity;
import com.example.hits.domain.entity.taskanswer.TaskAnswerStatus;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerCommentEntity;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
@ExtensionMethod({ TaskAnswerCommentMapper.class, SimpleUserMapper.class })
public class TaskAnswerMapper {

    private static final int MAX_POST_NAME_LENGTH = 30;

    public TaskAnswer toDomain(TaskAnswerEntity taskAnswerEntity) {
        return new TaskAnswer()
                .setId(taskAnswerEntity.getId())
                .setScore(taskAnswerEntity.getScore())
                .setSubmittedAt(taskAnswerEntity.getSubmittedAt())
                .setStatus(parseStatus(taskAnswerEntity))
                .setFileEntityIds(safeFiles(taskAnswerEntity).stream()
                        .map(FileEntity::getId)
                        .toList())
                .setCommentIds(safeComments(taskAnswerEntity).stream()
                        .map(TaskAnswerCommentEntity::getId)
                        .toList())
                .setPostId(taskAnswerEntity.getPostEntity() != null ? taskAnswerEntity.getPostEntity().getId() : null)
                .setUserId(taskAnswerEntity.getUserEntity() != null ? taskAnswerEntity.getUserEntity().getId(): null);
    }

    public TaskAnswerModel toModel(TaskAnswerEntity taskAnswerEntity) {
        String postText = extractPostText(taskAnswerEntity);
        postText = postText.length() > MAX_POST_NAME_LENGTH ? postText.substring(0, MAX_POST_NAME_LENGTH) + "..." : postText;

        return new TaskAnswerModel()
                .setId(taskAnswerEntity.getId())
                .setScore(taskAnswerEntity.getScore())
                .setMaxScore(taskAnswerEntity.getPostEntity() != null ? taskAnswerEntity.getPostEntity().getMaxScore() : null)
                .setSubmittedAt(taskAnswerEntity.getSubmittedAt())
                .setStatus(parseStatus(taskAnswerEntity))
                .setFiles(safeFiles(taskAnswerEntity).stream()
                        .map(file -> new FileModel(file.getId(), "answer"))
                        .toList())
                .setComments(safeComments(taskAnswerEntity).stream()
                        .map(TaskAnswerCommentMapper::toModel)
                        .toList())
                .setPostName(postText)
                .setPostId(taskAnswerEntity.getPostEntity() != null ? taskAnswerEntity.getPostEntity().getId() : null)
                .setCourseId(taskAnswerEntity.getPostEntity() != null && taskAnswerEntity.getPostEntity().getCourseEntity() != null
                        ? taskAnswerEntity.getPostEntity().getCourseEntity().getId() : null);
    }

    public TaskAnswerFullModel toFullModel(TaskAnswerEntity taskAnswerEntity) {
        String postText = extractPostText(taskAnswerEntity);

        return new TaskAnswerFullModel()
                .setId(taskAnswerEntity.getId())
                .setScore(taskAnswerEntity.getScore())
                .setMaxScore(taskAnswerEntity.getPostEntity() != null ? taskAnswerEntity.getPostEntity().getMaxScore() : null)
                .setSubmittedAt(taskAnswerEntity.getSubmittedAt())
                .setStatus(parseStatus(taskAnswerEntity))
                .setFiles(safeFiles(taskAnswerEntity).stream()
                        .map(file -> new FileModel(file.getId(), "answer"))
                        .toList())
                .setComments(safeComments(taskAnswerEntity).stream()
                        .map(TaskAnswerCommentMapper::toModel)
                        .toList())
                .setPostName(postText.substring(0, Math.min(MAX_POST_NAME_LENGTH, postText.length())))
                .setPostId(taskAnswerEntity.getPostEntity() != null ? taskAnswerEntity.getPostEntity().getId() : null)
                .setUser(taskAnswerEntity.getUserEntity() != null ? taskAnswerEntity.getUserEntity().toModel() : null)
                .setCourseId(taskAnswerEntity.getPostEntity() != null && taskAnswerEntity.getPostEntity().getCourseEntity() != null
                        ? taskAnswerEntity.getPostEntity().getCourseEntity().getId() : null);
    }

    private TaskAnswerStatus parseStatus(TaskAnswerEntity taskAnswerEntity) {
        LocalDateTime submittedAt = taskAnswerEntity.getSubmittedAt();

        if (submittedAt == null) {
            LocalDateTime createdAt = taskAnswerEntity.getPostEntity() != null ? taskAnswerEntity.getPostEntity().getCreatedAt() : null;
            return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusDays(7))
                    ? TaskAnswerStatus.NEW
                    : TaskAnswerStatus.NOT_COMPLETED;
        }

        LocalDateTime deadline = taskAnswerEntity.getPostEntity() != null ? taskAnswerEntity.getPostEntity().getDeadline() : null;
        return deadline == null || !submittedAt.isAfter(deadline)
                ? TaskAnswerStatus.COMPLETED
                : TaskAnswerStatus.COMPETED_AFTER_DEADLINE;
    }

    private String extractPostText(TaskAnswerEntity taskAnswerEntity) {
        if (taskAnswerEntity.getPostEntity() == null || taskAnswerEntity.getPostEntity().getText() == null) {
            return "";
        }

        return taskAnswerEntity.getPostEntity().getText();
    }

    private List<FileEntity> safeFiles(TaskAnswerEntity taskAnswerEntity) {
        return taskAnswerEntity.getFileEntities() != null ? taskAnswerEntity.getFileEntities() : List.of();
    }

    private List<TaskAnswerCommentEntity> safeComments(TaskAnswerEntity taskAnswerEntity) {
        return taskAnswerEntity.getComments() != null ? taskAnswerEntity.getComments() : List.of();
    }
}
