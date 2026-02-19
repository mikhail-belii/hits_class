package com.example.hits.domain.mapper;

import com.example.hits.application.model.comment.PostCommentModel;
import com.example.hits.domain.entity.postcomment.PostComment;
import com.example.hits.domain.entity.user.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PostCommentMapperTests {

    @Test
    void toModel_nullAuthor_shouldHandleNullAuthor() {
        PostComment entity = new PostComment();
        entity.setAuthor(null);

        PostCommentModel result = PostCommentMapper.toModel(entity);

        assertNull(result.getAuthor());
    }

    @Test
    void toModel_fullCommentData_shouldMapAllFieldsCorrectly() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail("user@aaa.com");

        PostComment entity = new PostComment();
        entity.setId(postId);
        entity.setText("Скебоб момент");
        entity.setAuthor(user);
        entity.setCreatedAt(LocalDateTime.now().minusDays(1));
        entity.setUpdatedAt(LocalDateTime.now());

        PostCommentModel result = PostCommentMapper.toModel(entity);

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
        PostComment entity = new PostComment();

        PostCommentModel result = PostCommentMapper.toModel(entity);

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
                PostCommentMapper.toModel(null)
        );
    }
}
