package com.example.hits.application.controller;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.model.auth.RefreshTokenRequestModel;
import com.example.hits.application.model.auth.TokenResponseModel;
import com.example.hits.application.model.common.ResponseModel;
import com.example.hits.application.model.user.UserLoginModel;
import com.example.hits.application.model.user.UserModel;
import com.example.hits.application.model.user.UserRegisterModel;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.application.service.AuthService;
import com.example.hits.application.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/user")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "User was successfully registered",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponseModel.class)
                    )}),
            @ApiResponse(responseCode = "400",
                    description = "Bad request",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseModel.class)
                    )})
    })
    public TokenResponseModel register(@Valid @RequestBody UserRegisterModel userRegisterModel) throws ExceptionWrapper {
        return authService.register(userRegisterModel);
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "User successfully logged in",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponseModel.class)
                    )}),
            @ApiResponse(responseCode = "400",
                    description = "Bad request",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseModel.class)
                    )})
    })
    public TokenResponseModel login(@Valid @RequestBody UserLoginModel userLoginModel) throws ExceptionWrapper {
        return  authService.login(userLoginModel);
    }

    @PostMapping("/refresh-tokens")
    @Operation(summary = "Refresh tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Tokens ware refreshed",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponseModel.class)
                    )}),
            @ApiResponse(responseCode = "400",
                    description = "Bad request",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseModel.class)
                    )})
    })
    public TokenResponseModel refreshTokens(@Valid @RequestBody RefreshTokenRequestModel model) throws ExceptionWrapper {
        return authService.refreshTokens(model.getRefreshToken());
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Logout")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Logout succeed")
    })
    public void logout(@RequestAttribute("userId") String userId,
                       @RequestAttribute("accessToken") String accessToken) throws ExceptionWrapper {
        authService.logout(UUID.fromString(userId), accessToken);
    }
}
