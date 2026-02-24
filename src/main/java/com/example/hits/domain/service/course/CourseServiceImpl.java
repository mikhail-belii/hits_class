package com.example.hits.domain.service.course;

import com.example.hits.application.model.course.CourseCreateModel;
import com.example.hits.application.model.course.CourseEditModel;
import com.example.hits.application.model.course.CourseShortModel;
import com.example.hits.application.model.course.UserCourseModel;
import com.example.hits.application.repository.CourseRepository;
import com.example.hits.application.service.CourseService;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.mapper.CourseMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseCodeGenerator courseCodeGenerator;

    public void createCourse(UUID requestingUserId, CourseCreateModel courseCreateModel) {

    }

    public void editCourse(UUID requestingUserId, UUID courseId, CourseEditModel courseEditModel) {

    }

    public void archiveCourse(UUID requestingUserId, boolean isArchived, UUID courseId){

    }

    public List<UserCourseModel> getCourseUsers(UUID requestingUserId, UUID courseId) {
        return null;
    }

    public List<CourseShortModel> getUserCourses(UUID requestingUserId, boolean isArchived) {
        return null;
    }

    public void joinCourseByCode(UUID requestingUserId, String code) {

    }

    public void changeUserRoleOnCourse(
            UUID requestingUserId,
            UUID courseId,
            UUID userId,
            UserCourseRole newUserRole
    ) {

    }

    public void removeUserFromCourse(
            UUID requestingUserId,
            UUID courseId,
            UUID userId
    ) {

    }

}
