package com.example.hits.domain.mapper;

import com.example.hits.application.model.course.CourseModel;
import com.example.hits.application.model.course.CourseShortModel;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CourseMapperTests {

    @Test
    void toModel_whenCourseHasAllFields_returnsCourseModelWithAllFields() {
        UUID id = UUID.randomUUID();
        String name = "Test Course";
        String description = "Test Description";
        LocalDateTime createdAt = LocalDateTime.now();
        String joinCode = "ABC123";
        Boolean isArchived = false;
        UserCourseRole userCourseRole = UserCourseRole.HEAD_TEACHER;

        Course courseEntity = createCourseWithFields(
                id, name, description, createdAt, joinCode, isArchived);

        CourseModel result = CourseMapper.toModel(courseEntity, userCourseRole);

        assertEquals(id, result.getId());
        assertEquals(name, result.getName());
        assertEquals(description, result.getDescription());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(joinCode, result.getJoinCode());
        assertEquals(isArchived, result.getIsArchived());
        assertEquals(userCourseRole, result.getCurrentUserCourseRole());
    }

    @Test
    void toModel_whenCourseHasNullFields_returnsCourseModelWithNullFields() {
        Course courseEntity = createCourseWithFields(
                UUID.randomUUID(), null, null, null, null, null);

        CourseModel result = CourseMapper.toModel(courseEntity, null);

        assertNotNull(result.getId());
        assertNull(result.getName());
        assertNull(result.getDescription());
        assertNull(result.getCreatedAt());
        assertNull(result.getJoinCode());
        assertNull(result.getIsArchived());
        assertNull(result.getCurrentUserCourseRole());
    }

    @Test
    void toShortModel_whenCourseHasAllFields_returnsCourseShortModelWithAllFields() {
        UUID id = UUID.randomUUID();
        String name = "Test Course";
        String description = "Test Description";

        Course courseEntity = createCourseWithFields(
                id, name, description, LocalDateTime.now(), "CODE", false);

        CourseShortModel result = CourseMapper.toShortModel(courseEntity);

        assertEquals(id, result.getId());
        assertEquals(name, result.getName());
        assertEquals(description, result.getDescription());
    }

    @Test
    void toShortModel_whenCourseHasNullFields_returnsCourseShortModelWithNullFields() {
        UUID id = UUID.randomUUID();
        Course courseEntity = createCourseWithFields(
                id, null, null, LocalDateTime.now(), "CODE", false
        );

        CourseShortModel result = CourseMapper.toShortModel(courseEntity);

        assertNotNull(result.getId());
        assertNull(result.getName());
        assertNull(result.getDescription());
    }

    private static Course createCourseWithFields(
            UUID id,
            String name,
            String description,
            LocalDateTime createdAt,
            String joinCode,
            Boolean isArchived
    ) {
        Course course = new Course();
        course.setId(id);
        course.setName(name);
        course.setDescription(description);
        course.setCreatedAt(createdAt);
        course.setJoinCode(joinCode);
        course.setIsArchived(isArchived);
        return course;
    }
}
