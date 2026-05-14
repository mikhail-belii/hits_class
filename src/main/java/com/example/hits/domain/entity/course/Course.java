package com.example.hits.domain.entity.course;

import com.example.hits.infrastructure.persistence.entity.UserCourseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class Course {

    private UUID id;

    private String name;

    private String joinCode;

    private CourseMarkEvaluationType courseMarkEvaluationType;

    private Float passThreshold;

    private String description;

    private List<UUID> courseUsers;

    private LocalDateTime createdAt;
}