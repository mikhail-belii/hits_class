package com.example.hits.domain.entity.post;

import com.example.hits.infrastructure.persistence.entity.CourseEntity;
import com.example.hits.infrastructure.persistence.entity.FileEntity;
import com.example.hits.infrastructure.persistence.entity.PostCommentEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class Post {

    private UUID id;

    private String text;

    private LocalDateTime updatedAt;

    private UUID courseId;

    private UUID authorId;

    private List<UUID> fileEntityIds;

    private List<UUID> commentIds;

    private PostType postType;

    private LocalDateTime createdAt;

    private LocalDateTime deadline;

    private Integer maxScore;

}
