package com.example.hits.application.model.course;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@Accessors(chain=true)
public class CourseEditModel {

    @NotNull
    @Size(min=3, max=128)
    private String name;

    @NotNull
    @Size(min=3, max=512)
    private String description;

}
