package com.example.hits.application.service;

import com.example.hits.application.handler.ExceptionWrapper;

public interface LoggedOutTokenService {
    void addLoggedOutToken(String token) throws ExceptionWrapper;
    Boolean isTokenLoggedOut(String token) throws ExceptionWrapper;
}
