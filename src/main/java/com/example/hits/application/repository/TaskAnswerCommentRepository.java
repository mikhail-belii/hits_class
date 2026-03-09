package com.example.hits.application.repository;

import com.example.hits.domain.entity.taskanswercomment.TaskAnswerComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskAnswerCommentRepository extends JpaRepository<TaskAnswerComment, UUID> {
}
