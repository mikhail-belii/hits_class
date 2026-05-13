package com.example.hits.application.mapper;

import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import com.example.hits.infrastructure.persistence.entity.UserCourseEntity;
import com.example.hits.presentation.dto.course.UserCourseModel;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

@UtilityClass
@ExtensionMethod(SimpleUserMapper.class)
public class UserCourseMapper {

    public UserCourse toDomain(UserCourseEntity userCourseEntity) {
        return UserCourse.restore(
                userCourseEntity.getId(),
                userCourseEntity.getCourseEntity().getId(),
                userCourseEntity.getUserEntity().getId(),
                userCourseEntity.getUserRole(),
                userCourseEntity.getCreatedAt(),
                userCourseEntity.getScore()
        );
    }

    public UserCourseModel toModel(UserCourseEntity userCourseEntity) {
        Float score = userCourseEntity.getUserRole() == UserCourseRole.STUDENT
                ? userCourseEntity.getScore()
                : null;
        return new UserCourseModel()
                .setUserModel(userCourseEntity.getUserEntity().toModel())
                .setUserRole(userCourseEntity.getUserRole())
                .setScore(score);
    }
}
