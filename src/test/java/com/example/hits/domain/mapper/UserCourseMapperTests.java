package com.example.hits.domain.mapper;

import com.example.hits.application.model.course.CourseModel;
import com.example.hits.application.model.course.CourseShortModel;
import com.example.hits.application.model.course.UserCourseModel;
import com.example.hits.application.model.user.UserModel;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserCourseMapperTests {

    @Test
    void toModel_whenUserCourseHasAllFields_returnsUserCourseModelWithAllFields() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        UserCourseRole userCourseRole = UserCourseRole.HEAD_TEACHER;

        UserCourse userCourseEntity = createUserCourseWithFields(
                UUID.randomUUID(),
                user,
                new Course(),
                userCourseRole,
                LocalDateTime.now());

        UserCourseModel result = UserCourseMapper.toModel(userCourseEntity);

        assertEquals(user.getId(), result.getUserModel().getId());
        assertEquals(user.getEmail(), result.getUserModel().getEmail());
        assertEquals(userCourseRole, result.getUserRole());
    }

    @Test
    void toModel_whenUserIsNull_throwsNullPointerException() {
        UserCourse userCourseEntity = createUserCourseWithFields(
                null,
                null,
                null,
                null,
                null);

        assertThrows(NullPointerException.class,
                () -> UserCourseMapper.toModel(userCourseEntity));
    }

    private static UserCourse createUserCourseWithFields(
            UUID id,
            User user,
            Course course,
            UserCourseRole userCourseRole,
            LocalDateTime createdAt
    ) {
        UserCourse userCourse = new UserCourse();
        userCourse.setId(id);
        userCourse.setUser(user);
        userCourse.setCourse(course);
        userCourse.setUserRole(userCourseRole);
        userCourse.setCreatedAt(createdAt);
        return userCourse;
    }
}
