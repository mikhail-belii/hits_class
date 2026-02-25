package com.example.hits.application.service;

import com.example.hits.application.model.auth.TokenResponseModel;
import com.example.hits.application.model.user.UserLoginModel;
import com.example.hits.application.model.user.UserRegisterModel;

import java.util.UUID;

public interface AuthService {
    TokenResponseModel register(UserRegisterModel userRegisterModel);
    TokenResponseModel login(UserLoginModel userLoginModel);
    TokenResponseModel refreshTokens(String refreshToken);
    void logout(UUID userId, String accessToken);
}
