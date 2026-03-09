package com.example.hits.domain.mapper;

import com.example.hits.application.model.comment.postcomment.PostCommentModel;
import com.example.hits.application.model.comment.taskanswercomment.TaskAnswerCommentModel;
import com.example.hits.domain.entity.postcomment.PostComment;
import com.example.hits.domain.entity.taskanswercomment.TaskAnswerComment;
import com.example.hits.domain.entity.user.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TaskAnswerCommentMapperTests {

    @Test
    void toModel_nullAuthor_shouldHandleNullAuthor() {
        TaskAnswerComment entity = new TaskAnswerComment();
        entity.setAuthor(null);

        TaskAnswerCommentModel result = TaskAnswerCommentMapper.toModel(entity);

        assertNull(result.getAuthor());
    }

    @Test
    void toModel_fullCommentData_shouldMapAllFieldsCorrectly() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("user@aaa.com");

        TaskAnswerComment entity = new TaskAnswerComment();
        entity.setId(postId);
        entity.setText("Скебоб момент");
        entity.setAuthor(user);
        entity.setCreatedAt(LocalDateTime.now().minusDays(1));
        entity.setUpdatedAt(LocalDateTime.now());

        TaskAnswerCommentModel result = TaskAnswerCommentMapper.toModel(entity);

        assertNotNull(result);
        assertEquals(postId, result.getId());
        assertEquals("Скебоб момент", result.getText());

        assertNotNull(result.getAuthor());
        assertEquals(userId, result.getAuthor().getId());
        assertEquals("user@aaa.com", result.getAuthor().getEmail());

        assertEquals(entity.getCreatedAt(), result.getCreatedAt());
        assertEquals(entity.getUpdatedAt(), result.getUpdatedAt());
    }

    @Test
    void toModel_emptyCommentData_shouldHandleNullFields() {
        TaskAnswerComment entity = new TaskAnswerComment();

        TaskAnswerCommentModel result = TaskAnswerCommentMapper.toModel(entity);

        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getText());
        assertNull(result.getAuthor());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }

    @Test
    void toModel_shouldThrowException_whenEntityIsNull() {
        assertThrows(NullPointerException.class, () ->
                TaskAnswerCommentMapper.toModel(null)
        );
    }
}
