package com.example.hits.domain.service.auth;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.security.auth.message.AuthException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoggedOutTokenServiceImplTest {
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private LoggedOutTokenServiceImpl loggedOutTokenServiceImpl;

    @Test
    public void addLoggedOutToken_validToken_tokenIsMarkedAsLoggedOut() {
        var token = "access_token";
        var claims = org.mockito.Mockito.mock(Claims.class);

        when(jwtUtil.parseAccessClaims(token)).thenReturn(claims);
        when(claims.get("token_id", String.class)).thenReturn("token_id_1");

        loggedOutTokenServiceImpl.addLoggedOutToken(token);

        Assertions.assertTrue(loggedOutTokenServiceImpl.isTokenLoggedOut(token));
    }

    @Test
    public void isTokenLoggedOut_tokenWasNotAdded_returnsFalse() {
        var token = "access_token";
        var claims = org.mockito.Mockito.mock(Claims.class);

        when(jwtUtil.parseAccessClaims(token)).thenReturn(claims);
        when(claims.get("token_id", String.class)).thenReturn("token_id_2");

        Assertions.assertFalse(loggedOutTokenServiceImpl.isTokenLoggedOut(token));
    }

    @Test
    public void addLoggedOutToken_tokenWithoutTokenId_throwsExceptionWrapper() {
        var token = "access_token";
        var claims = org.mockito.Mockito.mock(Claims.class);

        when(jwtUtil.parseAccessClaims(token)).thenReturn(claims);
        when(claims.get("token_id", String.class)).thenReturn(null);

        var ex = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> loggedOutTokenServiceImpl.addLoggedOutToken(token)
        );

        Assertions.assertEquals(AuthException.class, ex.getExceptionClass());
        Assertions.assertEquals("Invalid token", ex.getErrors().get("Auth"));
    }

    @Test
    public void isTokenLoggedOut_tokenWithoutTokenId_throwsExceptionWrapper() {
        var token = "access_token";
        var claims = org.mockito.Mockito.mock(Claims.class);

        when(jwtUtil.parseAccessClaims(token)).thenReturn(claims);
        when(claims.get("token_id", String.class)).thenReturn(null);

        var ex = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> loggedOutTokenServiceImpl.isTokenLoggedOut(token)
        );

        Assertions.assertEquals(AuthException.class, ex.getExceptionClass());
        Assertions.assertEquals("Invalid token", ex.getErrors().get("Auth"));
    }
}
