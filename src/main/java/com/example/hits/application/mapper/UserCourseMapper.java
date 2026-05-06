package com.example.hits.application.mapper;

import com.example.hits.domain.entity.usercourse.UserCourse;
import com.example.hits.presentation.dto.course.UserCourseModel;
import com.example.hits.infrastructure.persistence.entity.UserCourseEntity;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

@UtilityClass
@ExtensionMethod(SimpleUserMapper.class)
public class UserCourseMapper {

    public UserCourse toDomain(UserCourseEntity userCourseEntity) {
        return new UserCourse()
                .setId(userCourseEntity.getId())
                .setCourseId(userCourseEntity.getCourseEntity().getId())
                .setUserId(userCourseEntity.getUserEntity().getId())
                .setCreatedAt(userCourseEntity.getCreatedAt())
                .setUserRole(userCourseEntity.getUserRole());
    }

    public UserCourseModel toModel(UserCourseEntity userCourseEntity) {
        return new UserCourseModel()
                .setUserModel(userCourseEntity.getUserEntity().toModel())
                .setUserRole(userCourseEntity.getUserRole());
    }
}
