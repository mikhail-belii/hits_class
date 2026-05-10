package com.example.hits.domain.entity.taskanswer;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TaskAnswer {

    private UUID id;

    private Float score;

    private LocalDateTime submittedAt;

    private TaskAnswerStatus status;

    private List<UUID> fileEntityIds;

    private List<UUID> commentIds;

    private UUID userId;

    private UUID postId;

}
