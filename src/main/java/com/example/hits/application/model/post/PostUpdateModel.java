package com.example.hits.application.model.post;

import com.example.hits.application.model.file.FileModel;
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
