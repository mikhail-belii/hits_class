package com.example.hits.application.repository;

import com.example.hits.domain.entity.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
}
