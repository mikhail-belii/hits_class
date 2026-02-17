package com.example.hits.domain.entity.taskanswercomment;

import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_answer_comment")
@Data
@Accessors(chain = true)
public class TaskAnswerComment {

    @Id
    private UUID id;

    @NotNull
    @Length(max = 2048)
    private String text;

    @ManyToOne
    @JoinColumn(name = "task_answer_id")
    private TaskAnswer taskAnswer;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @NotNull
    private LocalDateTime createdAt;

}