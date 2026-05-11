package com.example.hits.application.mapper;

import com.example.hits.infrastructure.persistence.entity.CourseEntity;
import com.example.hits.presentation.dto.course.CourseModel;
import com.example.hits.presentation.dto.course.CourseShortModel;
import com.example.hits.domain.entity.user.UserCourseRole;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CourseMapper {

    public CourseModel toModel(CourseEntity courseEntity, UserCourseRole userCourseRole) {
        return new CourseModel()
                .setId(courseEntity.getId())
                .setName(courseEntity.getName())
                .setDescription(courseEntity.getDescription())
                .setCreatedAt(courseEntity.getCreatedAt())
                .setJoinCode(courseEntity.getJoinCode())
                .setCourseMarkEvaluationType(courseEntity.getCourseMarkEvaluationType())
                .setPassThreshold(courseEntity.getPassThreshold())
                .setIsArchived(courseEntity.getIsArchived())
                .setCurrentUserCourseRole(userCourseRole);
    }

    public CourseShortModel toShortModel(CourseEntity courseEntity, UserCourseRole currentUserCourseRole) {
        return new CourseShortModel()
                .setId(courseEntity.getId())
                .setName(courseEntity.getName())
                .setDescription(courseEntity.getDescription())
                .setCurrentUserCourseRole(currentUserCourseRole);
    }
}
