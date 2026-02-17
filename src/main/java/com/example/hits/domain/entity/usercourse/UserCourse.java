package com.example.hits.domain.entity.usercourse;

import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_course")
@Data
@Accessors(chain = true)
public class UserCourse {

    @Id
    private UUID id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @Enumerated(EnumType.STRING)
    @NotNull
    private UserCourseRole userRole;

    @NotNull
    private LocalDateTime createdAt;
}