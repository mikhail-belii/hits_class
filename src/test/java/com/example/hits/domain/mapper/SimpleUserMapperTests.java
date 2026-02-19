package com.example.hits.domain.mapper;

import com.example.hits.application.model.user.UserModel;
import com.example.hits.domain.entity.user.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleUserMapperTests {

    @Test
    void toModel_userWithData_shouldMapFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setEmail("test@example.com");

        UserModel result = SimpleUserMapper.toModel(user);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void toModel_emptyUserData_shouldHandleNullFields() {
        User user = new User();

        UserModel result = SimpleUserMapper.toModel(user);

        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getEmail());
    }
}
