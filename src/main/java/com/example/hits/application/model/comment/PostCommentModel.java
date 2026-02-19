package com.example.hits.application.model.comment;

import com.example.hits.application.model.user.UserModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain=true)
public class PostCommentModel {

    private UUID id;

    private String text;

    private UserModel author;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
