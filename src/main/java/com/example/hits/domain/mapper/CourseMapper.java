package com.example.hits.domain.mapper;

import com.example.hits.application.model.course.CourseModel;
import com.example.hits.application.model.course.CourseShortModel;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.user.UserCourseRole;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CourseMapper {

    public CourseModel toModel(Course courseEntity, UserCourseRole userCourseRole) {
        return new CourseModel()
                .setId(courseEntity.getId())
                .setName(courseEntity.getName())
                .setDescription(courseEntity.getDescription())
                .setCreatedAt(courseEntity.getCreatedAt())
                .setJoinCode(courseEntity.getJoinCode())
                .setIsArchived(courseEntity.getIsArchived())
                .setCurrentUserCourseRole(userCourseRole);
    }

    public CourseShortModel toShortModel(Course courseEntity, UserCourseRole currentUserCourseRole) {
        return new CourseShortModel()
                .setId(courseEntity.getId())
                .setName(courseEntity.getName())
                .setDescription(courseEntity.getDescription())
                .setCurrentUserCourseRole(currentUserCourseRole);
    }
}
