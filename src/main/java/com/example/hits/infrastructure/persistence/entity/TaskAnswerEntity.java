package com.example.hits.infrastructure.persistence.entity;

import com.example.hits.domain.entity.taskanswer.TaskAnswerStatus;
import com.example.hits.domain.entity.taskanswer.TaskAnswerEvaluationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "task_answer")
@Getter
@Setter
@Accessors(chain = true)
public class TaskAnswerEntity {

    @Id
    private UUID id = UUID.randomUUID();

    private Float score = 0f;

    private Float teacherScore = null;

    private LocalDateTime submittedAt = null;

    @Enumerated(EnumType.STRING)
    @NotNull
    private TaskAnswerStatus status = TaskAnswerStatus.NOT_COMPLETED;

    @Enumerated(EnumType.STRING)
    private TaskAnswerEvaluationStatus evaluationStatus = TaskAnswerEvaluationStatus.NOT_EVALUATED;

    @OneToMany(mappedBy = "taskAnswerEntity")
    private List<FileEntity> fileEntities = new ArrayList<>();

    @OneToMany(mappedBy = "taskAnswerEntity")
    private List<TaskAnswerCommentEntity> comments = new ArrayList<>();

    @OneToMany(mappedBy = "taskAnswerEntity")
    private List<CriteriaScoreEntity> criteriaScoreEntities;

    @OneToMany(mappedBy = "taskAnswerEntity")
    private List<TaskAnswerStudentAppraiserEntity> studentAppraiserEntities;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private PostEntity postEntity;

}
