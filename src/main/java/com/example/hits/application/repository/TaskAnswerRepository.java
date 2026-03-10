package com.example.hits.application.repository;

import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskAnswerRepository extends JpaRepository<TaskAnswer, UUID> {
    Optional<TaskAnswer> findByUserIdAndPostId(UUID userId, UUID postId);
    List<TaskAnswer> findAllByUserIdAndPostCourseId(UUID userId, UUID courseId);
}
