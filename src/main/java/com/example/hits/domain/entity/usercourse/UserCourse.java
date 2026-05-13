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

    private Float score;

    private LocalDateTime createdAt;

    public static UserCourse restore(UUID id,
                                     UUID courseId,
                                     UUID userId,
                                     UserCourseRole userRole,
                                     LocalDateTime createdAt,
                                     Float score) {
        return new UserCourse()
                .setId(id)
                .setCourseId(courseId)
                .setUserId(userId)
                .setUserRole(userRole)
                .setCreatedAt(createdAt)
                .setScore(score);
    }
}
