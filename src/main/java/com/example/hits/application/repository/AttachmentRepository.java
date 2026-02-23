package com.example.hits.application.repository;

import com.example.hits.domain.entity.attachment.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    boolean existsByFile_Id(UUID fileId);
    boolean existsByFile_IdAndPost_IdNot(UUID fileId, UUID postId);
}
