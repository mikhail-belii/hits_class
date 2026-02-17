package com.example.hits.domain.entity.user;

import com.example.hits.domain.entity.usercourse.UserCourse;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user")
@Data
@Accessors(chain = true)
public class User {

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

    @OneToMany(mappedBy = "user")
    private List<UserCourse> userCourses;

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