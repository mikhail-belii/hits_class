package com.example.hits.domain.entity.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@Accessors(chain = true)
public class User {
    @Id
    private UUID id;
    @NotNull
    @Length(min = 2, max = 255)
    public String firstName;
    @NotNull
    @Length(min = 2, max = 255)
    public String lastName;
    @NotNull
    @Email
    public String email;
    @NotNull
    public LocalDate birthday;
    @NotNull
    public String city;
    @Enumerated(EnumType.STRING)
    @NotNull
    public UserRole role;
    @Column(length = 400)
    public String refreshToken;
    public Instant refreshTokenExpiryDate;
    @NotNull
    public String passwordHash;
}