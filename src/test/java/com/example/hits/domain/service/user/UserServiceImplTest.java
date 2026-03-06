package com.example.hits.domain.service.user;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.model.user.UserModel;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserProfile_existingUser_returnsMappedModel() {
        UUID userId = UUID.randomUUID();
        User user = new User()
                .setId(userId)
                .setFirstName("Ivan")
                .setLastName("Petrov")
                .setEmail("ivan@example.com")
                .setBirthday(LocalDate.of(2000, 1, 1))
                .setCity("Moscow");
        UserModel expectedModel = new UserModel()
                .setId(userId)
                .setFirstName("Ivan")
                .setLastName("Petrov")
                .setEmail("ivan@example.com")
                .setBirthday(LocalDate.of(2000, 1, 1))
                .setCity("Moscow");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toModel(user)).thenReturn(expectedModel);

        UserModel result = userService.getUserProfile(userId);

        Assertions.assertSame(expectedModel, result);
        verify(userRepository).findById(userId);
        verify(userMapper).toModel(user);
    }

    @Test
    void getUserProfile_missingUser_throwsEntityNotFoundException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> userService.getUserProfile(userId)
        );

        Assertions.assertEquals(EntityNotFoundException.class, exception.getExceptionClass());
        Assertions.assertEquals("User not found", exception.getErrors().get("userId"));
    }
}
