package com.example.hits.presentation.request.course;

import com.example.hits.domain.entity.course.CourseMarkEvaluationType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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

    @NotNull
    private CourseMarkEvaluationType courseMarkEvaluationType;

    @Min(0)
    private Float passThreshold;

}
