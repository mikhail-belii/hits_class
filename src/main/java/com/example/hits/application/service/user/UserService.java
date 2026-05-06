package com.example.hits.application.service.user;

import com.example.hits.presentation.dto.user.UserModel;
import com.example.hits.infrastructure.persistence.repository.UserRepository;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.application.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserModel getUserProfile(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);

        return userMapper.toModel(user);
    }
}
