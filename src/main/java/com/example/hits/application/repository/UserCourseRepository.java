package com.example.hits.application.repository;

import com.example.hits.domain.entity.usercourse.UserCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserCourseRepository extends JpaRepository<UserCourse, UUID> {

}
