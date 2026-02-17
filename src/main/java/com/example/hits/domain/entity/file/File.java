package com.example.hits.domain.entity.file;

import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "file")
@Data
@Accessors(chain = true)
public class File {
    @Id
    private UUID id;

    @NotNull
    @Length(max = 256)
    private String path;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "task_answer_id")
    private TaskAnswer taskAnswer;

    @NotNull
    private LocalDateTime createdAt;
}