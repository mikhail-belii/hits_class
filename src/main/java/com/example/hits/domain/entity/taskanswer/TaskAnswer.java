package com.example.hits.domain.entity.taskanswer;

import com.example.hits.domain.entity.file.File;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.taskanswercomment.TaskAnswerComment;
import com.example.hits.domain.entity.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "task_answer")
@Data
@Accessors(chain = true)
public class TaskAnswer {

    @Id
    private UUID id = UUID.randomUUID();

    private Integer score = 0;

    private LocalDateTime submittedAt = null;

    @Enumerated(EnumType.STRING)
    @NotNull
    private TaskAnswerStatus status = TaskAnswerStatus.NOT_COMPLETED;

    @OneToMany(mappedBy = "taskAnswer")
    private List<File> files = new ArrayList<>();

    @OneToMany(mappedBy = "taskAnswer")
    private List<TaskAnswerComment> comments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

}
