package com.example.hits.infrastructure.persistence.repository;

import com.example.hits.infrastructure.persistence.entity.UserCourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserCourseRepository extends JpaRepository<UserCourseEntity, UUID> {

}
