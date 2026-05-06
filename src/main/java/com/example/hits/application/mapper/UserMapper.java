package com.example.hits.application.mapper;

import com.example.hits.presentation.dto.user.UserModel;
import com.example.hits.presentation.request.user.UserRegisterModel;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "refreshTokenExpiryDate", ignore = true)
    UserEntity toEntity(UserRegisterModel userRegisterModel);
    UserModel toModel(UserEntity userEntity);
}
