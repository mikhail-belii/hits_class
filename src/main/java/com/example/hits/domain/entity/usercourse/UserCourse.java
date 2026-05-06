package com.example.hits.domain.entity.usercourse;

import com.example.hits.domain.entity.user.UserCourseRole;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class UserCourse {

    private UUID id;

    private UUID userId;

    private UUID courseId;

    private UserCourseRole userRole;

    private LocalDateTime createdAt;
}