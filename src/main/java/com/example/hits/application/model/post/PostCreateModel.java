package com.example.hits.application.model.post;

import com.example.hits.application.model.file.FileModel;
import com.example.hits.domain.entity.post.PostType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Accessors(chain=true)
public class PostCreateModel {

    private String text;

    private List<FileModel> files;

    private PostType postType;

    private Integer maxScore;

    private LocalDateTime deadline;
}
