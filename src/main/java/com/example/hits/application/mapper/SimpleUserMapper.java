package com.example.hits.application.mapper;

import com.example.hits.infrastructure.persistence.entity.UserEntity;
import com.example.hits.presentation.dto.user.UserModel;
import lombok.experimental.UtilityClass;

/*
Выполняет ту же функцию, что и UserMapper, но без нахождения в DI
 */
@UtilityClass
public class SimpleUserMapper {

    public UserModel toModel(UserEntity userEntity) {
        return new UserModel()
                .setId(userEntity.getId())
                .setFirstName(userEntity.getFirstName())
                .setLastName(userEntity.getLastName())
                .setCity(userEntity.getCity())
                .setBirthday(userEntity.getBirthday())
                .setEmail(userEntity.getEmail());
    }
}
