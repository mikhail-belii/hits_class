package com.example.hits.domain.mapper;

import com.example.hits.application.model.taskanswer.TaskAnswerModel;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TaskAnswerMapper {

    public TaskAnswerModel toModel(TaskAnswer taskAnswer) {
        return new TaskAnswerModel()
                .setId(taskAnswer.getId());
    }
}
