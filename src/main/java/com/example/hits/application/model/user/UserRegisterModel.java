package com.example.hits.application.model.user;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class UserRegisterModel {
    @Email
    @NotBlank
    @NotNull
    public String email;
    @NotBlank
    @NotNull
    @Size(min = 8, max = 32)
    public String password;
    @NotNull
    @Size(min = 2, max = 255)
    public String firstName;
    @NotNull
    @Size(min = 2, max = 255)
    public String lastName;
    @NotNull
    public LocalDate birthday;
    @NotNull
    @NotBlank
    @Size(min = 2, max = 255)
    public String city;
}
