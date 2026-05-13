package com.example.hits.infrastructure.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user")
@Getter
@Setter
@Accessors(chain = true)
public class UserEntity {

    @Id
    private UUID id;

    @NotNull
    @Length(min = 2, max = 128)
    private String firstName;

    @NotNull
    @Length(min = 2, max = 128)
    private String lastName;

    @NotNull
    @Email
    private String email;

    @OneToMany(mappedBy = "userEntity")
    private List<UserCourseEntity> userCoursEntities;

    @NotNull
    private LocalDate birthday;

    @NotNull
    private String city;

    @Column(length = 400)
    private String refreshToken;

    private Instant refreshTokenExpiryDate;

    @NotNull
    private String passwordHash;

    @NotNull
    private LocalDateTime createdAt;
}