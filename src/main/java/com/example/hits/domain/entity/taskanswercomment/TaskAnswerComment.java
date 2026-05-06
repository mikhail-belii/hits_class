package com.example.hits.domain.entity.taskanswercomment;

import com.example.hits.infrastructure.persistence.entity.TaskAnswerEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TaskAnswerComment {

    private UUID id;

    private String text;

    private TaskAnswerEntity taskAnswerEntity;

    private UserEntity author;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}