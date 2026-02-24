package com.example.hits.domain.entity.post;

import com.example.hits.domain.entity.attachment.Attachment;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.file.File;
import com.example.hits.domain.entity.postcomment.PostComment;
import com.example.hits.domain.entity.user.User;
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
public class Post {

    @Id
    private UUID id;

    @NotNull
    @Length(max = 2048)
    private String text;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments;

    @OneToMany(mappedBy = "post")
    private List<PostComment> comments;

    @Enumerated(EnumType.STRING)
    @NotNull
    private PostType postType;

    @NotNull
    private LocalDateTime createdAt;

    private LocalDateTime deadline;

    private Integer maxScore;

}