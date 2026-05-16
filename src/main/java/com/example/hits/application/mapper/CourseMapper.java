package com.example.hits.application.mapper;

import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.infrastructure.persistence.entity.CourseEntity;
import com.example.hits.infrastructure.persistence.entity.UserCourseEntity;
import com.example.hits.presentation.dto.course.CourseModel;
import com.example.hits.presentation.dto.course.CourseShortModel;
import lombok.experimental.UtilityClass;

import java.util.stream.Collectors;

@UtilityClass
public class CourseMapper {

    public Course toDomain(CourseEntity courseEntity) {
        return new Course()
                .setId(courseEntity.getId())
                .setName(courseEntity.getName())
                .setDescription(courseEntity.getDescription())
                .setCourseUsers(courseEntity.getCourseUsers().stream().map(UserCourseEntity::getId).toList())
                .setCourseMarkEvaluationType(courseEntity.getCourseMarkEvaluationType())
                .setPassThreshold(courseEntity.getPassThreshold())
                .setCreatedAt(courseEntity.getCreatedAt())
                .setJoinCode(courseEntity.getJoinCode());
    }

    public CourseModel toModel(CourseEntity courseEntity, UserCourseEntity currentUserCourse) {
        UserCourseRole role = currentUserCourse.getUserRole();
        Float score = role == UserCourseRole.STUDENT ? currentUserCourse.getScore() : null;
        return new CourseModel()
                .setId(courseEntity.getId())
                .setName(courseEntity.getName())
                .setDescription(courseEntity.getDescription())
                .setCreatedAt(courseEntity.getCreatedAt())
                .setJoinCode(courseEntity.getJoinCode())
                .setCourseMarkEvaluationType(courseEntity.getCourseMarkEvaluationType())
                .setPassThreshold(courseEntity.getPassThreshold())
                .setIsArchived(courseEntity.getIsArchived())
                .setCurrentUserCourseRole(role)
                .setScore(score);
    }

    public CourseShortModel toShortModel(CourseEntity courseEntity, UserCourseRole currentUserCourseRole) {
        return new CourseShortModel()
                .setId(courseEntity.getId())
                .setName(courseEntity.getName())
                .setDescription(courseEntity.getDescription())
                .setCurrentUserCourseRole(currentUserCourseRole);
    }
}
