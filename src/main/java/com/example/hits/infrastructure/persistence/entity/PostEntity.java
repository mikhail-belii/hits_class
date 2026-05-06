package com.example.hits.infrastructure.persistence.entity;

import com.example.hits.domain.entity.post.PostType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "post")
@Data
@Accessors(chain = true)
public class PostEntity {

    @Id
    private UUID id;

    @NotNull
    @Length(max = 2048)
    private String text;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private CourseEntity courseEntity;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private UserEntity author;

    @OneToMany(mappedBy = "postEntity")
    private List<FileEntity> fileEntities;

    @OneToMany(mappedBy = "postEntity")
    private List<PostCommentEntity> comments;

    @Enumerated(EnumType.STRING)
    @NotNull
    private PostType postType;

    @NotNull
    private LocalDateTime createdAt;

    private LocalDateTime deadline;

    private Integer maxScore;

}
