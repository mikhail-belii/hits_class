package com.example.hits.application.util;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.security.auth.message.AuthException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class JwtUtilTest {
    private final String jwtAccessSecret = "dmVyeS1zdHJvbmctc2VjcmV0LWZvci1hY2Nlc3MtdG9rZW4tMDAx";
    private final String jwtRefreshSecret = "dmVyeS1zdHJvbmctc2VjcmV0LWZvci1yZWZyZXNoLXRva2VuLTAwMg==";
    private final long accessLifetimeMinutes = 10;
    private final long refreshLifetimeDays = 10;
    private JwtUtil jwtUtil;
    private User testUser;

    @BeforeEach
    public void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil,
                "jwtAccessSecret", Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret)));
        ReflectionTestUtils.setField(jwtUtil,
                "jwtRefreshSecret", Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtRefreshSecret)));
        ReflectionTestUtils.setField(jwtUtil,
                "accessLifetimeMinutes", accessLifetimeMinutes);
        ReflectionTestUtils.setField(jwtUtil,
                "refreshLifetimeDays", refreshLifetimeDays);

        testUser = new User()
                .setId(UUID.randomUUID());
    }

    @Test
    public void generateAccessToken_containsExpectedClaims() {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret));

        var token = jwtUtil.generateAccessToken(testUser);

        var claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        var userId = claims.get("user_id", String.class);
        var userRole = claims.get("role", String.class);
        var tokenId = claims.get("token_id", String.class);

        Assertions.assertEquals(testUser.getId().toString(), userId);
        Assertions.assertNotNull(tokenId);
    }

    @Test
    public void generateRefreshToken_containsExpectedClaims() {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtRefreshSecret));

        var token = jwtUtil.generateRefreshToken(testUser);

        var claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        var userId = claims.get("user_id", String.class);

        Assertions.assertEquals(testUser.getId().toString(), userId);
    }

    @Test
    public void parseAccessClaims_validAccessToken_returnsClaims() {
        var token = jwtUtil.generateAccessToken(testUser);

        Claims claims = jwtUtil.parseAccessClaims(token);

        Assertions.assertEquals(testUser.getId().toString(), claims.get("user_id", String.class));
    }

    @Test
    public void parseRefreshClaims_validRefreshToken_returnsClaims() {
        var token = jwtUtil.generateRefreshToken(testUser);

        Claims claims = jwtUtil.parseRefreshClaims(token);

        Assertions.assertEquals(testUser.getId().toString(), claims.get("user_id", String.class));
    }

    @Test
    public void getAccessTokenExpirationDate_whenCalled_returnsFutureDateWithConfiguredLifetime() {
        Instant beforeCall = Instant.now();

        Instant expirationDate = jwtUtil.getAccessTokenExpirationDate();

        Instant expectedMin = beforeCall.plus(accessLifetimeMinutes, ChronoUnit.MINUTES).minusSeconds(2);
        Instant expectedMax = beforeCall.plus(accessLifetimeMinutes, ChronoUnit.MINUTES).plusSeconds(2);

        Assertions.assertFalse(expirationDate.isBefore(expectedMin));
        Assertions.assertFalse(expirationDate.isAfter(expectedMax));
    }

    @Test
    public void getRefreshTokenExpirationDate_whenCalled_returnsFutureDateWithConfiguredLifetime() {
        Instant beforeCall = Instant.now();

        Instant expirationDate = jwtUtil.getRefreshTokenExpirationDate();

        Instant expectedMin = beforeCall.plus(refreshLifetimeDays, ChronoUnit.DAYS).minusSeconds(2);
        Instant expectedMax = beforeCall.plus(refreshLifetimeDays, ChronoUnit.DAYS).plusSeconds(2);

        Assertions.assertFalse(expirationDate.isBefore(expectedMin));
        Assertions.assertFalse(expirationDate.isAfter(expectedMax));
    }

    @Test
    public void getUserIdFromRefreshToken_validToken_returnsUserId() throws ExceptionWrapper {
        var refreshToken = jwtUtil.generateRefreshToken(testUser);

        UUID userId = jwtUtil.getUserIdFromRefreshToken(refreshToken);

        Assertions.assertEquals(testUser.getId(), userId);
    }

    @Test
    public void getUserIdFromRefreshToken_noUserIdClaim_throwsExceptionWrapper() {
        var refreshTokenWithoutUserId = Jwts.builder()
                .claim("other_claim", "value")
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtRefreshSecret)))
                .compact();

        var ex = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> jwtUtil.getUserIdFromRefreshToken(refreshTokenWithoutUserId)
        );

        Assertions.assertEquals(AuthException.class, ex.getExceptionClass());
        Assertions.assertEquals("Invalid token", ex.getErrors().get("Auth"));
    }
}
