package com.example.hits.application.service;

import com.example.hits.application.model.course.CourseCreateModel;
import com.example.hits.application.model.course.CourseEditModel;
import com.example.hits.application.model.course.CourseShortModel;
import com.example.hits.application.model.course.UserCourseModel;
import com.example.hits.domain.entity.user.UserCourseRole;

import java.util.List;
import java.util.UUID;

public interface CourseService {
    void createCourse(UUID requestingUserId, CourseCreateModel courseCreateModel);

    void editCourse(UUID requestingUserId, UUID courseId, CourseEditModel courseEditModel);

    void archiveCourse(UUID requestingUserId, boolean isArchived, UUID courseId);

    List<UserCourseModel> getCourseUsers(UUID requestingUserId, UUID courseId);

    List<CourseShortModel> getUserCourses(UUID requestingUserId, boolean isArchived);

    void joinCourseByCode(UUID requestingUserId, String code);

    void changeUserRoleOnCourse(
            UUID requestingUserId,
            UUID courseId,
            UUID userId,
            UserCourseRole newUserRole);

    void removeUserFromCourse(
            UUID requestingUserId,
            UUID courseId,
            UUID userId);
}
