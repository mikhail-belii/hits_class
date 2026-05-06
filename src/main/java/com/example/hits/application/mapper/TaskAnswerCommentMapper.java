package com.example.hits.application.mapper;

import com.example.hits.presentation.dto.comment.taskanswercomment.TaskAnswerCommentModel;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerCommentEntity;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

@UtilityClass
@ExtensionMethod(SimpleUserMapper.class)
public class TaskAnswerCommentMapper {

    public TaskAnswerCommentModel toModel(TaskAnswerCommentEntity taskAnswerCommentEntity) {
        return new TaskAnswerCommentModel()
                .setId(taskAnswerCommentEntity.getId())
                .setText(taskAnswerCommentEntity.getText())
                .setAuthor(taskAnswerCommentEntity.getAuthor() != null
                        ? taskAnswerCommentEntity.getAuthor().toModel()
                        : null)
                .setCreatedAt(taskAnswerCommentEntity.getCreatedAt())
                .setUpdatedAt(taskAnswerCommentEntity.getUpdatedAt());
    }
}
