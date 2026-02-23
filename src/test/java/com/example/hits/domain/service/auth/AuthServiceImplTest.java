package com.example.hits.domain.service.auth;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.model.user.UserLoginModel;
import com.example.hits.application.model.user.UserRegisterModel;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.application.service.LoggedOutTokenService;
import com.example.hits.application.util.JwtUtil;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private LoggedOutTokenService loggedOutTokenService;
    @InjectMocks
    private AuthServiceImpl authServiceImpl;

    @Test
    public void register_freeEmail_returnsTokenResponse() throws ExceptionWrapper {
        var userRegisterModel = new UserRegisterModel();
        userRegisterModel.setEmail("email@mail.ru");
        userRegisterModel.setPassword("password");
        when(userRepository.existsByEmail(userRegisterModel.getEmail()))
                .thenReturn(false);
        when(jwtUtil.generateAccessToken(Mockito.any()))
                .thenReturn("token");
        when(jwtUtil.generateRefreshToken(Mockito.any()))
                .thenReturn("refresh_token");
        when(userMapper.toEntity(Mockito.any()))
                .thenReturn(new User());
        when(passwordEncoder.encode(Mockito.any()))
                .thenReturn("password");

        var response = authServiceImpl.register(userRegisterModel);

        Assertions.assertTrue(response.getAccessToken().equals("token")
                            && response.getRefreshToken().equals("refresh_token"));
        verify(userRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    public void register_emailInUse_throwsExceptionWrapper() {
        var userRegisterModel = new UserRegisterModel();
        userRegisterModel.setEmail("email@mail.ru");
        userRegisterModel.setPassword("password");

        when(userRepository.existsByEmail(userRegisterModel.getEmail()))
                .thenReturn(true);

        var ex = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> authServiceImpl.register(userRegisterModel));

        Assertions.assertEquals(org.apache.coyote.BadRequestException.class, ex.getExceptionClass());
        Assertions.assertEquals("User with this email already exists", ex.getErrors().get("Email"));

        verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void login_correctEmailAndPassword_returnsTokenResponse() throws ExceptionWrapper {
        var userLoginModel = new UserLoginModel();
        userLoginModel.setEmail("email@mail.ru");
        userLoginModel.setPassword("password");
        when(userRepository.findByEmail(userLoginModel.getEmail()))
                .thenReturn(Optional.of(new User().setEmail(userLoginModel.getEmail()).setPasswordHash("password")));
        when(passwordEncoder.matches(Mockito.any(), Mockito.any()))
                .thenReturn(true);
        when(jwtUtil.generateAccessToken(Mockito.any()))
                .thenReturn("token");
        when(jwtUtil.generateRefreshToken(Mockito.any()))
                .thenReturn("refresh_token");

        var response = authServiceImpl.login(userLoginModel);

        Assertions.assertTrue(response.getAccessToken().equals("token")
                && response.getRefreshToken().equals("refresh_token"));
        verify(userRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    public void login_emailNotFound_throwsExceptionWrapper() {
        var userLoginModel = new UserLoginModel();
        userLoginModel.setEmail("email@mail.ru");
        userLoginModel.setPassword("password");

        when(userRepository.findByEmail(userLoginModel.getEmail()))
                .thenReturn(Optional.empty());

        var ex = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> authServiceImpl.login(userLoginModel));

        Assertions.assertEquals(org.apache.coyote.BadRequestException.class, ex.getExceptionClass());
        Assertions.assertEquals("Incorrect password or email", ex.getErrors().get("Credentials"));

        verify(passwordEncoder, Mockito.never()).matches(Mockito.any(), Mockito.any());
        verify(jwtUtil, Mockito.never()).generateAccessToken(Mockito.any());
        verify(jwtUtil, Mockito.never()).generateRefreshToken(Mockito.any());
        verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void login_wrongPassword_throwsExceptionWrapper() {
        var userLoginModel = new UserLoginModel();
        userLoginModel.setEmail("email@mail.ru");
        userLoginModel.setPassword("wrong_password");

        when(userRepository.findByEmail(userLoginModel.getEmail()))
                .thenReturn(Optional.of(new User().setEmail(userLoginModel.getEmail()).setPasswordHash("password")));
        when(passwordEncoder.matches(userLoginModel.getPassword(), "password"))
                .thenReturn(false);

        var ex = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> authServiceImpl.login(userLoginModel));

        Assertions.assertEquals(org.apache.coyote.BadRequestException.class, ex.getExceptionClass());
        Assertions.assertEquals("Incorrect password or email", ex.getErrors().get("Credentials"));

        verify(jwtUtil, Mockito.never()).generateAccessToken(Mockito.any());
        verify(jwtUtil, Mockito.never()).generateRefreshToken(Mockito.any());
        verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void refreshTokens_invalidRefreshToken_throwsExceptionWrapper() {
        var refreshToken = "bad_refresh_token";

        when(jwtUtil.parseRefreshClaims(refreshToken))
                .thenThrow(new RuntimeException("Invalid token"));

        var ex = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> authServiceImpl.refreshTokens(refreshToken));

        Assertions.assertEquals(org.apache.coyote.BadRequestException.class, ex.getExceptionClass());
        Assertions.assertEquals("Invalid refresh token", ex.getErrors().get("Refresh Token"));

        verify(userRepository, Mockito.never()).findById(Mockito.any());
        verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void refreshTokens_userNotFound_throwsExceptionWrapper() throws ExceptionWrapper {
        var refreshToken = "refresh_token";
        var userId = UUID.randomUUID();

        when(jwtUtil.parseRefreshClaims(refreshToken))
                .thenReturn(Mockito.mock(Claims.class));
        when(jwtUtil.getUserIdFromRefreshToken(refreshToken))
                .thenReturn(userId);
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        var ex = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> authServiceImpl.refreshTokens(refreshToken));

        Assertions.assertEquals(jakarta.persistence.EntityNotFoundException.class, ex.getExceptionClass());
        Assertions.assertEquals("User not found", ex.getErrors().get("Not found"));

        verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void refreshTokens_refreshTokenMismatch_throwsExceptionWrapper() throws ExceptionWrapper {
        var refreshToken = "refresh_token";
        var userId = UUID.randomUUID();
        var user = new User()
                .setId(userId)
                .setRefreshToken("other_refresh_token")
                .setRefreshTokenExpiryDate(Instant.now().plusSeconds(3600));

        when(jwtUtil.parseRefreshClaims(refreshToken))
                .thenReturn(Mockito.mock(Claims.class));
        when(jwtUtil.getUserIdFromRefreshToken(refreshToken))
                .thenReturn(userId);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        var ex = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> authServiceImpl.refreshTokens(refreshToken));

        Assertions.assertEquals(org.apache.coyote.BadRequestException.class, ex.getExceptionClass());
        Assertions.assertEquals("Invalid or expired refresh token", ex.getErrors().get("Refresh Token"));

        verify(jwtUtil, Mockito.never()).generateAccessToken(Mockito.any());
        verify(jwtUtil, Mockito.never()).generateRefreshToken(Mockito.any());
        verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void refreshTokens_expiredRefreshToken_throwsExceptionWrapper() throws ExceptionWrapper {
        var refreshToken = "refresh_token";
        var userId = UUID.randomUUID();
        var user = new User()
                .setId(userId)
                .setRefreshToken(refreshToken)
                .setRefreshTokenExpiryDate(Instant.now().minusSeconds(60));

        when(jwtUtil.parseRefreshClaims(refreshToken))
                .thenReturn(Mockito.mock(Claims.class));
        when(jwtUtil.getUserIdFromRefreshToken(refreshToken))
                .thenReturn(userId);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        var ex = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> authServiceImpl.refreshTokens(refreshToken));

        Assertions.assertEquals(org.apache.coyote.BadRequestException.class, ex.getExceptionClass());
        Assertions.assertEquals("Invalid or expired refresh token", ex.getErrors().get("Refresh Token"));

        verify(jwtUtil, Mockito.never()).generateAccessToken(Mockito.any());
        verify(jwtUtil, Mockito.never()).generateRefreshToken(Mockito.any());
        verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void refreshTokens_validRefreshToken_returnsTokenResponse() throws ExceptionWrapper {
        var refreshToken = "refresh_token";
        var userId = UUID.randomUUID();
        var user = new User()
                .setId(userId)
                .setRefreshToken(refreshToken)
                .setRefreshTokenExpiryDate(Instant.now().plusSeconds(3600));
        var newExpiryDate = Instant.now().plusSeconds(7200);

        when(jwtUtil.parseRefreshClaims(refreshToken))
                .thenReturn(Mockito.mock(Claims.class));
        when(jwtUtil.getUserIdFromRefreshToken(refreshToken))
                .thenReturn(userId);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(user))
                .thenReturn("new_access_token");
        when(jwtUtil.generateRefreshToken(user))
                .thenReturn("new_refresh_token");
        when(jwtUtil.getRefreshTokenExpirationDate())
                .thenReturn(newExpiryDate);

        var response = authServiceImpl.refreshTokens(refreshToken);

        Assertions.assertEquals("new_access_token", response.getAccessToken());
        Assertions.assertEquals("new_refresh_token", response.getRefreshToken());
        Assertions.assertEquals("new_refresh_token", user.getRefreshToken());
        Assertions.assertEquals(newExpiryDate, user.getRefreshTokenExpiryDate());

        verify(userRepository, Mockito.times(1)).save(user);
    }

    @Test
    public void logout_userNotFound_throwsExceptionWrapper() throws ExceptionWrapper {
        var userId = UUID.randomUUID();
        var accessToken = "access_token";

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        var ex = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> authServiceImpl.logout(userId, accessToken));

        Assertions.assertEquals(jakarta.persistence.EntityNotFoundException.class, ex.getExceptionClass());
        Assertions.assertEquals("User not found", ex.getErrors().get("Not found"));

        verify(loggedOutTokenService, Mockito.never()).addLoggedOutToken(Mockito.any());
        verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void logout_existingUser_clearsRefreshTokenAndSavesUser() throws ExceptionWrapper {
        var userId = UUID.randomUUID();
        var accessToken = "access_token";
        var user = new User()
                .setId(userId)
                .setRefreshToken("refresh_token")
                .setRefreshTokenExpiryDate(Instant.now().plusSeconds(3600));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        authServiceImpl.logout(userId, accessToken);

        Assertions.assertNull(user.getRefreshToken());
        Assertions.assertNull(user.getRefreshTokenExpiryDate());
        verify(loggedOutTokenService, Mockito.times(1)).addLoggedOutToken(accessToken);
        verify(userRepository, Mockito.times(1)).save(user);
    }
}
