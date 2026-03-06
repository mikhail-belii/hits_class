package com.example.hits.application.service;

import com.example.hits.application.model.user.UserModel;

import java.util.UUID;

public interface UserService {
    UserModel getUserProfile(UUID userId);
}
