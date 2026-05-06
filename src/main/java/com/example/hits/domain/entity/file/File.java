package com.example.hits.domain.entity.file;

import com.example.hits.infrastructure.persistence.entity.PostEntity;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class File {

    private UUID id;

    private String path;

    private String originalName;

    private UserEntity uploader;

    private PostEntity postEntity;

    private TaskAnswerEntity taskAnswerEntity;

    private LocalDateTime createdAt;
}
