package com.example.hits.domain.entity.taskanswer;

import com.example.hits.domain.entity.attachment.Attachment;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.file.File;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.domain.entity.postcomment.PostComment;
import com.example.hits.domain.entity.taskanswercomment.TaskAnswerComment;
import com.example.hits.domain.entity.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "task_answer")
@Data
@Accessors(chain = true)
public class TaskAnswer {

    @Id
    private UUID id;

    private Integer score;

    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @NotNull
    private TaskAnswerStatus status;

    @OneToMany(mappedBy = "taskAnswer")
    private List<Attachment> attachments;

    @OneToMany(mappedBy = "taskAnswer")
    private List<TaskAnswerComment> comments;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

}