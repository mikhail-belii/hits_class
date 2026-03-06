package com.example.hits.domain.service.user;

import com.example.hits.application.model.user.UserModel;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.application.service.UserService;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.domain.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserModel getUserProfile(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);

        return userMapper.toModel(user);
    }
}
