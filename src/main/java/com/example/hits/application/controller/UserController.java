package com.example.hits.application.controller;

import com.example.hits.application.model.common.ResponseModel;
import com.example.hits.application.model.user.UserModel;
import com.example.hits.application.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get my profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Profile was retrieved",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserModel.class)
                    )}),
            @ApiResponse(responseCode = "400",
                    description = "Bad request",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseModel.class)
                    )})
    })
    public UserModel getMyProfile(@RequestAttribute("userId") String userId) {
        return userService.getUserProfile(UUID.fromString(userId));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get my profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Profile was retrieved",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserModel.class)
                    )}),
            @ApiResponse(responseCode = "400",
                    description = "Bad request",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseModel.class)
                    )})
    })
    public UserModel getUserProfile(@PathVariable String userId) {
        return userService.getUserProfile(UUID.fromString(userId));
    }
}
