package com.example.hits.domain.mapper;

import com.example.hits.application.model.comment.postcomment.PostCommentModel;
import com.example.hits.application.model.post.PostModel;
import com.example.hits.application.model.comment.PostCommentModel;
import com.example.hits.application.model.post.PostShortModel;
import com.example.hits.domain.entity.attachment.Attachment;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.domain.entity.postcomment.PostComment;
import com.example.hits.domain.entity.user.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PostMapperTests {

    @Test
    void toModel_fullData_shouldMapAllFieldsCorrectly() {
        UUID postId = UUID.randomUUID();
        UUID postAuthorId = UUID.randomUUID();
        UUID comment1Id = UUID.randomUUID();
        UUID comment2Id = UUID.randomUUID();
        UUID commentAuthorId = UUID.randomUUID();

        User author = new User();
        author.setId(postAuthorId);
        author.setEmail("auth@or.com");

        User commentAuthor = new User();
        commentAuthor.setId(commentAuthorId);
        commentAuthor.setEmail("firstcomment@example.com");

        PostComment comment1 = new PostComment();
        comment1.setId(comment1Id);
        comment1.setText("Первый");
        comment1.setAuthor(commentAuthor);
        comment1.setCreatedAt(LocalDateTime.now().minusHours(2));
        comment1.setUpdatedAt(LocalDateTime.now().minusHours(1));

        PostComment comment2 = new PostComment();
        comment2.setId(comment2Id);
        comment2.setText("Второй");
        comment2.setAuthor(commentAuthor);
        comment2.setCreatedAt(LocalDateTime.now().minusHours(3));
        comment2.setUpdatedAt(LocalDateTime.now().minusHours(2));

        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime deadline = LocalDateTime.now().plusDays(7);

        List<Attachment> attachments = new ArrayList<>();
        PostType postType = PostType.TASK;

        Post post = new Post();
        post.setId(postId);
        post.setText("Всем привет! Начинаем перекличку");
        post.setAuthor(author);
        post.setAttachments(attachments);
        post.setPostType(postType);
        post.setCreatedAt(createdAt);
        post.setDeadline(deadline);
        post.setMaxScore(100);
        post.setComments(List.of(comment1, comment2));

        PostShortModel model = PostMapper.toModel(post);

        assertNotNull(model);
        assertEquals(postId, model.getId());
        assertEquals("Всем привет! Начинаем перекличку", model.getText());

        assertNotNull(model.getAuthor());
        assertEquals(postAuthorId, model.getAuthor().getId());
        assertEquals("auth@or.com", model.getAuthor().getEmail());

        assertEquals(postType, model.getPostType());
        assertEquals(createdAt, model.getCreatedAt());
        assertEquals(deadline, model.getDeadline());
        assertEquals(100, model.getMaxScore());

        assertNotNull(model.getComments());
        assertEquals(2, model.getComments().size());

        PostCommentModel commentModel1 = model.getComments().getFirst();
        assertEquals(comment1Id, commentModel1.getId());
        assertEquals("Первый", commentModel1.getText());
        assertNotNull(commentModel1.getAuthor());
        assertEquals(commentAuthorId, commentModel1.getAuthor().getId());
        assertEquals("firstcomment@example.com", commentModel1.getAuthor().getEmail());

        PostCommentModel commentModel2 = model.getComments().get(1);
        assertEquals(comment2Id, commentModel2.getId());
        assertEquals("Второй", commentModel2.getText());
        assertNotNull(commentModel2.getAuthor());
        assertEquals(commentAuthorId, commentModel2.getAuthor().getId());
        assertEquals("firstcomment@example.com", commentModel2.getAuthor().getEmail());
    }

    @Test
    void toModel_emptyAuthor_mappedAuthorIsNull() {
        UUID postId = UUID.randomUUID();
        Post post = new Post();
        post.setId(postId);
        post.setText("x");
        post.setAuthor(null);
        post.setComments(List.of());

        PostShortModel model = PostMapper.toModel(post);

        assertNotNull(model);
        assertNull(model.getAuthor());
    }

    @Test
    void toModel_emptyCommentList_shouldMapToEmptyList() {
        UUID id = UUID.randomUUID();
        User author = new User();
        author.setId(id);
        author.setEmail("abcd@abcd.com");

        Post post = new Post();
        post.setId(id);
        post.setText("привет");
        post.setAuthor(author);
        post.setComments(List.of());

        PostShortModel model = PostMapper.toModel(post);

        assertNotNull(model.getComments());
        assertTrue(model.getComments().isEmpty());
    }

    @Test
    void toModel_shouldThrowException_whenPostIsNull() {
        assertThrows(NullPointerException.class, () -> PostMapper.toModel(null));
    }
}
