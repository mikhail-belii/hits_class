package com.example.hits.application.model.post;

import com.example.hits.application.model.comment.PostCommentModel;
import com.example.hits.application.model.file.FileModel;
import com.example.hits.application.model.user.UserModel;
import com.example.hits.domain.entity.post.PostType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain=true)
public class PostModel {

    private UUID id;

    private String text;

    private UserModel author;

    private List<FileModel> files;

    private PostType postType;

    private LocalDateTime createdAt;

    private LocalDateTime deadline;

    private Integer maxScore;

    private List<PostCommentModel> comments;
}
