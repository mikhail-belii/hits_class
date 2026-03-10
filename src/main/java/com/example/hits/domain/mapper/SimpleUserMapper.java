package com.example.hits.domain.mapper;

import com.example.hits.application.model.user.UserModel;
import com.example.hits.domain.entity.user.User;
import lombok.experimental.UtilityClass;

/*
Выполняет ту же функцию, что и UserMapper, но без нахождения в DI
 */
@UtilityClass
public class SimpleUserMapper {

    public UserModel toModel(User userEntity) {
        return new UserModel()
                .setId(userEntity.getId())
                .setFirstName(userEntity.getFirstName())
                .setLastName(userEntity.getLastName())
                .setCity(userEntity.getCity())
                .setBirthday(userEntity.getBirthday())
                .setEmail(userEntity.getEmail());
    }
}
