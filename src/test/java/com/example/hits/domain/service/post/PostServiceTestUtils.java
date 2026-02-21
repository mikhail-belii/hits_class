package com.example.hits.domain.service.post;

import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PostServiceTestUtils {
    public static User createUser(UUID id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    public static Course createCourseWithUserRole(User user, UserCourseRole role) {
        Course course = new Course();
        course.setId(UUID.randomUUID());
        course.setCreatedAt(LocalDateTime.now());

        UserCourse userCourse = new UserCourse();
        userCourse.setId(UUID.randomUUID());
        userCourse.setUser(user);
        userCourse.setUserRole(role);

        course.setCourseUsers(List.of(userCourse));
        return course;
    }
}
