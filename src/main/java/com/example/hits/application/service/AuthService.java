package com.example.hits.application.service;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.model.auth.TokenResponseModel;
import com.example.hits.application.model.user.UserLoginModel;
import com.example.hits.application.model.user.UserRegisterModel;

import java.util.UUID;

public interface AuthService {
    TokenResponseModel register(UserRegisterModel userRegisterModel) throws ExceptionWrapper;
    TokenResponseModel login(UserLoginModel userLoginModel) throws ExceptionWrapper;
    TokenResponseModel refreshTokens(String refreshToken) throws ExceptionWrapper;
    void logout(UUID userId, String accessToken) throws ExceptionWrapper;
}
