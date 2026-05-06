package com.example.hits.infrastructure.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "post_comment")
@Data
@Accessors(chain = true)
public class PostCommentEntity {

    @Id
    private UUID id;

    @NotNull
    @Length(max = 2048)
    private String text;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private PostEntity postEntity;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private UserEntity author;

    @NotNull
    private LocalDateTime createdAt;

}