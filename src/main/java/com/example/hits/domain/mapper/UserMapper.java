package com.example.hits.domain.mapper;

import com.example.hits.application.model.user.UserRegisterModel;
import com.example.hits.domain.entity.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "refreshTokenExpiryDate", ignore = true)
    User toEntity(UserRegisterModel userRegisterModel);
}
