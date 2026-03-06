package com.example.hits.domain.mapper;

import com.example.hits.application.model.comment.postcomment.PostCommentModel;
import com.example.hits.application.model.comment.taskanswercomment.TaskAnswerCommentModel;
import com.example.hits.domain.entity.postcomment.PostComment;
import com.example.hits.domain.entity.taskanswercomment.TaskAnswerComment;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

@UtilityClass
@ExtensionMethod(SimpleUserMapper.class)
public class TaskAnswerCommentMapper {

    public TaskAnswerCommentModel toModel(TaskAnswerComment taskAnswerComment) {
        return null;
    }
}
