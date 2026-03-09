package com.example.hits.application.repository;

import com.example.hits.domain.entity.postcomment.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {
}
