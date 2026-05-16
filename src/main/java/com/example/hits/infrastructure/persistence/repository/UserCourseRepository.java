package com.example.hits.infrastructure.persistence.repository;

import com.example.hits.infrastructure.persistence.entity.UserCourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserCourseRepository extends JpaRepository<UserCourseEntity, UUID> {

    @Query(value = """
        SELECT uc.id,
               uc.user_id,
               uc.course_id,
               uc.user_role,
               uc.score,
               uc.created_at
          FROM user_course uc
               JOIN course c
               ON uc.course_id = c.id
               JOIN post p
               ON p.course_id = c.id
               JOIN task_answer ta
               ON ta.post_id = p.id
               AND ta.id = :taskAnswerId
         WHERE uc.user_id = :userId;
    """, nativeQuery = true)
    Optional<UserCourseEntity> findByTaskAnswerIdAndUserId(UUID taskAnswerId, UUID userId);

    Optional<UserCourseEntity> findByUserEntityId(UUID userId);

    List<UserCourseEntity> findAllByCourseEntityId(UUID courseId);

}
