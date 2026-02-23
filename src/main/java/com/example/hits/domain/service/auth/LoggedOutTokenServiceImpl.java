package com.example.hits.domain.service.auth;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.service.LoggedOutTokenService;
import com.example.hits.application.util.JwtUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.security.auth.message.AuthException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@AllArgsConstructor
public class LoggedOutTokenServiceImpl implements LoggedOutTokenService {
    private final JwtUtil jwtUtil;
    private final Cache<String, String> loggedOutTokens = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofDays(30))
            .maximumSize(100_000)
            .build();


    @Override
    public void addLoggedOutToken(String token) throws ExceptionWrapper {
        var tokenId = getTokenId(token);
        loggedOutTokens.put(tokenId, token);
    }

    @Override
    public Boolean isTokenLoggedOut(String token) throws ExceptionWrapper {
        var tokenId = getTokenId(token);
        return loggedOutTokens.getIfPresent(tokenId) != null;
    }

    private String getTokenId(String token) throws ExceptionWrapper {
        var claims = jwtUtil.parseAccessClaims(token);

        var tokenId = claims.get("token_id",  String.class);
        if  (tokenId != null) {
            return tokenId;
        }
        var authEx = new ExceptionWrapper(new AuthException());
        authEx.addError("Auth", "Invalid token");
        throw authEx;
    }
}
