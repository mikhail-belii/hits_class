package com.example.hits.domain.service.auth;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.model.auth.TokenResponseModel;
import com.example.hits.application.model.user.UserLoginModel;
import com.example.hits.application.model.user.UserRegisterModel;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.application.service.AuthService;
import com.example.hits.application.service.LoggedOutTokenService;
import com.example.hits.application.util.JwtUtil;
import com.example.hits.domain.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final LoggedOutTokenService loggedOutTokenService;
    private final UserMapper userMapper;

    @Override
    public TokenResponseModel register(UserRegisterModel userRegisterModel) throws ExceptionWrapper {
        if (userRepository.existsByEmail(userRegisterModel.getEmail())) {
            var badRequestEx = new ExceptionWrapper(new BadRequestException());
            badRequestEx.addError("Email", "User with this email already exists");
            throw badRequestEx;
        }

        var user = userMapper.toEntity(userRegisterModel);
        user.setId(UUID.randomUUID())
                .setPasswordHash(passwordEncoder.encode(userRegisterModel.getPassword()))
                .setCreatedAt(LocalDateTime.now());
        var accessToken = jwtUtil.generateAccessToken(user);
        var refreshToken = jwtUtil.generateRefreshToken(user);
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiryDate(jwtUtil.getRefreshTokenExpirationDate());

        userRepository.save(user);

        return new TokenResponseModel(accessToken, refreshToken);
    }

    @Override
    public TokenResponseModel login(UserLoginModel userLoginModel) throws ExceptionWrapper {
        var userOpt = userRepository.findByEmail(userLoginModel.getEmail());

        var badRequestEx = new ExceptionWrapper(new BadRequestException());
        badRequestEx.addError("Credentials", "Incorrect password or email");

        if (userOpt.isEmpty()) {
            throw badRequestEx;
        }

        var user = userOpt.get();

        if (!passwordEncoder.matches(userLoginModel.getPassword(), user.getPasswordHash())) {
            throw badRequestEx;
        }

        var accessToken = jwtUtil.generateAccessToken(user);
        var refreshToken = jwtUtil.generateRefreshToken(user);
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiryDate(jwtUtil.getRefreshTokenExpirationDate());

        userRepository.save(user);

        return new TokenResponseModel(accessToken, refreshToken);
    }

    @Override
    public TokenResponseModel refreshTokens(String refreshToken) throws ExceptionWrapper {
        try {
            jwtUtil.parseRefreshClaims(refreshToken);
        } catch (Exception e) {
            ExceptionWrapper badRequestException = new ExceptionWrapper(new BadRequestException());
            badRequestException.addError("Refresh Token", "Invalid refresh token");
            throw badRequestException;
        }

        var userId = jwtUtil.getUserIdFromRefreshToken(refreshToken);
        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            var entityNotFoundEx = new ExceptionWrapper(new EntityNotFoundException());
            entityNotFoundEx.addError("Not found", "User not found");
            throw entityNotFoundEx;
        }

        var user = userOpt.get();
        var savedRefreshToken = user.getRefreshToken();
        var savedExpiryDate = user.getRefreshTokenExpiryDate();

        if (savedRefreshToken == null || savedExpiryDate == null || !savedRefreshToken.equals(refreshToken) || Date.from(savedExpiryDate).before(new Date())) {
            var badRequestEx = new ExceptionWrapper(new BadRequestException());
            badRequestEx.addError("Refresh Token", "Invalid or expired refresh token");
            throw badRequestEx;
        }

        var newAccessToken = jwtUtil.generateAccessToken(user);
        var newRefreshToken = jwtUtil.generateRefreshToken(user);

        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiryDate(jwtUtil.getRefreshTokenExpirationDate());

        userRepository.save(user);

        return new TokenResponseModel(newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(UUID userId, String accessToken) throws ExceptionWrapper {
        var userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            var entityNotFoundEx = new ExceptionWrapper(new EntityNotFoundException());
            entityNotFoundEx.addError("Not found", "User not found");
            throw  entityNotFoundEx;
        }

        var user =  userOpt.get();

        loggedOutTokenService.addLoggedOutToken(accessToken);

        user.setRefreshToken(null);
        user.setRefreshTokenExpiryDate(null);

        userRepository.save(user);
    }
}
