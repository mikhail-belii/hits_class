package com.example.hits.domain.entity.user;

import com.example.hits.infrastructure.persistence.entity.UserCourseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class User {

    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private List<UserCourseEntity> userCoursEntities;

    private LocalDate birthday;

    private String city;

    private String refreshToken;

    private Instant refreshTokenExpiryDate;

    private String passwordHash;

    private LocalDateTime createdAt;
}