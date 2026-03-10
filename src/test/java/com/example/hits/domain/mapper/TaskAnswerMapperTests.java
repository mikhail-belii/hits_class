package com.example.hits.domain.mapper;

import com.example.hits.application.model.taskanswer.TaskAnswerModel;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TaskAnswerMapperTests {

    @Test
    void toModel_withId_shouldMapIdCorrectly() {
        UUID id = UUID.randomUUID();
        TaskAnswer taskAnswer = new TaskAnswer().setId(id);

        TaskAnswerModel result = TaskAnswerMapper.toModel(taskAnswer);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void toModel_withNullId_shouldMapNullId() {
        TaskAnswer taskAnswer = new TaskAnswer().setId(null);

        TaskAnswerModel result = TaskAnswerMapper.toModel(taskAnswer);

        assertNotNull(result);
        assertNull(result.getId());
    }
}
