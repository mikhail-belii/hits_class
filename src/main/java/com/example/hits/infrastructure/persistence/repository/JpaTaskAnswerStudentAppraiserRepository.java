package com.example.hits.infrastructure.persistence.repository;

import com.example.hits.infrastructure.persistence.entity.TaskAnswerStudentAppraiserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaTaskAnswerStudentAppraiserRepository extends JpaRepository<TaskAnswerStudentAppraiserEntity, UUID> {

}
