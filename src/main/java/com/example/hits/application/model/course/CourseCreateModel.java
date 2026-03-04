package com.example.hits.application.model.course;

import com.example.hits.application.model.file.FileModel;
import com.example.hits.domain.entity.post.PostType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain=true)
public class CourseCreateModel {

    @NotNull
    @Size(min=3, max=128)
    private String name;

    @NotNull
    @Size(min=3, max=512)
    private String description;

}
