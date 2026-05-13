package com.example.hits.infrastructure.persistence.entity;

import com.example.hits.domain.entity.user.UserCourseRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_course")
@Getter
@Setter
@Accessors(chain = true)
public class UserCourseEntity {

    @Id
    private UUID id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "course_id")
    private CourseEntity courseEntity;

    @Enumerated(EnumType.STRING)
    @NotNull
    private UserCourseRole userRole;

    private Float score;

    @NotNull
    private LocalDateTime createdAt;
}