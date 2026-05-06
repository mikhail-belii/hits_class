package com.example.hits.infrastructure.persistence.repository;

import com.example.hits.infrastructure.persistence.entity.PostCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostCommentRepository extends JpaRepository<PostCommentEntity, UUID> {
}
