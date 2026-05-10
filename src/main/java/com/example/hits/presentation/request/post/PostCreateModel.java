package com.example.hits.presentation.request.post;

import com.example.hits.presentation.dto.file.FileModel;
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

    private Float maxScore;

    private LocalDateTime deadline;
}
