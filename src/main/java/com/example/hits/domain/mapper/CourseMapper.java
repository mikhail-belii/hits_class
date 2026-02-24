package com.example.hits.domain.mapper;

import com.example.hits.application.model.course.CourseModel;
import com.example.hits.application.model.course.CourseShortModel;
import com.example.hits.application.model.file.FileModel;
import com.example.hits.application.model.post.PostModel;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class CourseMapper {

    public CourseModel toModel(Course courseEntity) {
        return new CourseModel();
    }

    public CourseShortModel toShortModel(Course courseEntity) {
        return new CourseShortModel();
    }
}
