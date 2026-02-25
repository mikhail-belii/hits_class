package com.example.hits.domain.service.course;

import com.example.hits.application.model.course.CourseCreateModel;
import com.example.hits.application.model.course.CourseEditModel;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;

import java.util.UUID;

public class CourseServiceTestUtils {

    public static User createUser(UUID id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    public static Course createCourse(UUID id, String name, String description, Boolean isArchived) {
        Course course = new Course();
        course.setId(id);
        course.setName(name);
        course.setDescription(description);
        course.setIsArchived(isArchived);
        return course;
    }

    public static UserCourse createUserCourse(User user, Course course, UserCourseRole userCourseRole) {
        UserCourse userCourse = new UserCourse();
        userCourse.setId(UUID.randomUUID());
        userCourse.setUser(user);
        userCourse.setCourse(course);
        userCourse.setUserRole(userCourseRole);
        return userCourse;
    }

    public static CourseCreateModel createCourseCreateModel(String name, String description) {
        return new CourseCreateModel()
            .setName(name)
            .setDescription(description);
    }

    public static CourseEditModel createCourseEditModel(String name, String description) {
        return new CourseEditModel()
                .setName(name)
                .setDescription(description);
    }

}
