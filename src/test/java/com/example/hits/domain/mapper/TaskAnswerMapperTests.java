package com.example.hits.domain.mapper;

import com.example.hits.application.model.taskanswer.TaskAnswerModel;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.taskanswer.TaskAnswerStatus;
import com.example.hits.domain.entity.taskanswercomment.TaskAnswerComment;
import com.example.hits.domain.entity.user.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TaskAnswerMapperTests {

    private static final String LONG_POST_TEXT = "0123456789тест";
    private static final String SHORT_POST_TEXT = "бебе";
    private static final int MAX_SCORE = 100;

    @Test
    void toModel_withRecentPostAndNoSubmittedAt_shouldMapNewStatus() {
        UUID id = UUID.randomUUID();
        TaskAnswer taskAnswer = createTaskAnswer(id, LONG_POST_TEXT, LocalDateTime.now().minusDays(6), null, null);

        TaskAnswerModel result = TaskAnswerMapper.toModel(taskAnswer);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(MAX_SCORE, result.getMaxScore());
        assertEquals(TaskAnswerStatus.NEW, result.getStatus());
        assertEquals("0123456789", result.getPostName());
    }

    @Test
    void toModel_withOldPostAndNoSubmittedAt_shouldMapNotCompletedStatus() {
        TaskAnswer taskAnswer = createTaskAnswer(null, SHORT_POST_TEXT, LocalDateTime.now().minusDays(8), null, null);

        TaskAnswerModel result = TaskAnswerMapper.toModel(taskAnswer);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals(MAX_SCORE, result.getMaxScore());
        assertEquals(TaskAnswerStatus.NOT_COMPLETED, result.getStatus());
        assertEquals(SHORT_POST_TEXT, result.getPostName());
    }

    @Test
    void toModel_withSubmittedAtBeforeDeadline_shouldMapCompletedStatus() {
        LocalDateTime deadline = LocalDateTime.now().plusDays(1);
        TaskAnswer taskAnswer = createTaskAnswer(
                UUID.randomUUID(),
                LONG_POST_TEXT,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now(),
                deadline
        );

        TaskAnswerModel result = TaskAnswerMapper.toModel(taskAnswer);

        assertEquals(MAX_SCORE, result.getMaxScore());
        assertEquals(TaskAnswerStatus.COMPLETED, result.getStatus());
    }

    @Test
    void toModel_withSubmittedAtAfterDeadline_shouldMapCompletedAfterDeadlineStatus() {
        LocalDateTime deadline = LocalDateTime.now().minusHours(1);
        TaskAnswer taskAnswer = createTaskAnswer(
                UUID.randomUUID(),
                LONG_POST_TEXT,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now(),
                deadline
        );

        TaskAnswerModel result = TaskAnswerMapper.toModel(taskAnswer);

        assertEquals(MAX_SCORE, result.getMaxScore());
        assertEquals(TaskAnswerStatus.COMPETED_AFTER_DEADLINE, result.getStatus());
    }

    @Test
    void toModel_withComments_shouldMapComments() {
        UUID commentId = UUID.randomUUID();
        User author = new User()
                .setId(UUID.randomUUID())
                .setEmail("user@aaa.com")
                .setFirstName("Ivan")
                .setLastName("Ivanov")
                .setCity("Tomsk")
                .setBirthday(LocalDate.of(2000, 1, 1));
        TaskAnswerComment comment = new TaskAnswerComment()
                .setId(commentId)
                .setText("comment text")
                .setAuthor(author)
                .setCreatedAt(LocalDateTime.now().minusHours(2))
                .setUpdatedAt(LocalDateTime.now().minusHours(1));
        TaskAnswer taskAnswer = createTaskAnswer(
                UUID.randomUUID(),
                LONG_POST_TEXT,
                LocalDateTime.now().minusDays(6),
                null,
                null
        ).setComments(List.of(comment));

        TaskAnswerModel result = TaskAnswerMapper.toModel(taskAnswer);

        assertEquals(MAX_SCORE, result.getMaxScore());
        assertEquals(1, result.getComments().size());
        assertEquals(commentId, result.getComments().getFirst().getId());
        assertEquals("comment text", result.getComments().getFirst().getText());
        assertEquals(author.getId(), result.getComments().getFirst().getAuthor().getId());
        assertEquals(author.getEmail(), result.getComments().getFirst().getAuthor().getEmail());
    }

    private static TaskAnswer createTaskAnswer(
            UUID id,
            String postText,
            LocalDateTime createdAt,
            LocalDateTime submittedAt,
            LocalDateTime deadline
    ) {
        Post post = new Post()
                .setText(postText)
                .setCreatedAt(createdAt)
                .setDeadline(deadline)
                .setMaxScore(MAX_SCORE);

        return new TaskAnswer()
                .setId(id)
                .setPost(post)
                .setSubmittedAt(submittedAt);
    }
}
