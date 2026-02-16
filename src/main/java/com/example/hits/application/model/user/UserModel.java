package com.example.hits.application.model.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class UserModel {
    public UUID id;
    public String email;
}
