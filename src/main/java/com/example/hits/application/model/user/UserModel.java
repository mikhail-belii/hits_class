package com.example.hits.application.model.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class UserModel {
    public UUID id;
    private String firstName;
    private String lastName;
    private LocalDate birthday;
    private String city;
    public String email;
}
