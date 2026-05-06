package com.example.hits.infrastructure.persistence.repository;

import com.example.hits.domain.entity.post.PostType;
import com.example.hits.infrastructure.persistence.entity.CourseEntity;
import com.example.hits.infrastructure.persistence.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<PostEntity, UUID> {
    List<PostEntity> findAllByCourseEntityAndPostType(CourseEntity course, PostType postType);
}
