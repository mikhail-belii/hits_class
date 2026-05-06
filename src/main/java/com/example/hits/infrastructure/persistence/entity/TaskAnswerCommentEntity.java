package com.example.hits.infrastructure.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_answer_comment")
@Data
@Accessors(chain = true)
public class TaskAnswerCommentEntity {

    @Id
    private UUID id;

    @NotNull
    @Length(max = 2048)
    private String text;

    @ManyToOne
    @JoinColumn(name = "task_answer_id")
    private TaskAnswerEntity taskAnswerEntity;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private UserEntity author;

    @NotNull
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}