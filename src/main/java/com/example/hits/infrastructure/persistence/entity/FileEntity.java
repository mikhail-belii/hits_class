package com.example.hits.infrastructure.persistence.entity;

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
public class FileEntity {
    @Id
    private UUID id;
    @NotNull
    @Length(max = 256)
    private String path;
    @NotNull
    @Length(max = 256)
    private String originalName;
    @ManyToOne
    @JoinColumn(name = "uploader_id")
    private UserEntity uploader;
    @ManyToOne
    @JoinColumn(name = "post_id")
    private PostEntity postEntity;
    @ManyToOne
    @JoinColumn(name = "task_answer_id")
    private TaskAnswerEntity taskAnswerEntity;
    @NotNull
    private LocalDateTime createdAt;
}
