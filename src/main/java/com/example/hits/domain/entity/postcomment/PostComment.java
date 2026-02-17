package com.example.hits.domain.entity.postcomment;

import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.user.User;
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
public class PostComment {

    @Id
    private UUID id;

    @NotNull
    @Length(max = 2048)
    private String text;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @NotNull
    private LocalDateTime createdAt;

}