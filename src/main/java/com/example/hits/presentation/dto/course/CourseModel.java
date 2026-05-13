package com.example.hits.presentation.dto.course;

import com.example.hits.domain.entity.course.CourseMarkEvaluationType;
import com.example.hits.domain.entity.user.UserCourseRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain=true)
public class CourseModel {

    private UUID id;

    private String name;

    private String joinCode;

    private CourseMarkEvaluationType courseMarkEvaluationType;

    private Float passThreshold;

    private String description;

    private Boolean isArchived;

    private LocalDateTime createdAt;

    private UserCourseRole currentUserCourseRole;

    private Float score;
}
