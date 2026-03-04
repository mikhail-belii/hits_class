package com.example.hits.domain.mapper;

import com.example.hits.application.model.course.CourseModel;
import com.example.hits.application.model.course.UserCourseModel;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.usercourse.UserCourse;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

@UtilityClass
@ExtensionMethod(SimpleUserMapper.class)
public class UserCourseMapper {

    public UserCourseModel toModel(UserCourse userCourseEntity) {
        return new UserCourseModel()
                .setUserModel(userCourseEntity.getUser().toModel())
                .setUserRole(userCourseEntity.getUserRole());
    }
}
