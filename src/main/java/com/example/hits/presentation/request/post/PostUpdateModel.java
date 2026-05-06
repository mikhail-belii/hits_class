package com.example.hits.presentation.request.post;

import com.example.hits.presentation.dto.file.FileModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@Accessors(chain=true)
public class PostUpdateModel {
    private String text;

    private List<FileModel> files;
}
