package com.example.hits.domain.entity.postcomment;

import com.example.hits.infrastructure.persistence.entity.PostEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class PostComment {

    private UUID id;

    private String text;

    private LocalDateTime updatedAt;

    private PostEntity post;

    private UserEntity author;

    private LocalDateTime createdAt;

}