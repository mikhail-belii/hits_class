package com.example.hits.domain.mapper;

import com.example.hits.application.model.course.CourseModel;
import com.example.hits.application.model.course.CourseShortModel;
import com.example.hits.domain.entity.course.Course;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CourseMapper {

    public CourseModel toModel(Course courseEntity) {
        return new CourseModel()
                .setId(courseEntity.getId())
                .setName(courseEntity.getName())
                .setDescription(courseEntity.getDescription())
                .setCreatedAt(courseEntity.getCreatedAt())
                .setJoinCode(courseEntity.getJoinCode())
                .setIsArchived(courseEntity.getIsArchived());
    }

    public CourseShortModel toShortModel(Course courseEntity) {
        return new CourseShortModel()
                .setId(courseEntity.getId())
                .setName(courseEntity.getName())
                .setDescription(courseEntity.getDescription());
    }
}
