package com.example.hits.presentation.dto.post;

import com.example.hits.domain.entity.markCriteria.EvaluationFunction;
import com.example.hits.domain.entity.post.TaskMarkEvaluationType;
import com.example.hits.presentation.dto.comment.postcomment.PostCommentModel;
import com.example.hits.presentation.dto.file.FileModel;
import com.example.hits.presentation.dto.taskanswer.TaskAnswerModel;
import com.example.hits.presentation.dto.user.UserModel;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.domain.entity.post.TaskAnswerAppraisingType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain=true)
public class PostFullModel {

    private UUID id;

    private String text;

    private UserModel author;

    private List<FileModel> files;

    private PostType postType;

    private TaskMarkEvaluationType taskMarkEvaluationType;

    private LocalDateTime createdAt;

    private LocalDateTime deadline;

    private Float multiplier;

    private EvaluationFunction evaluationFunction;

    private Float minScore;

    private Float maxScore;

    private List<PostCommentModel> comments;

    private TaskAnswerModel taskAnswer;

    private LocalDateTime appraiserDeadline;

    private TaskAnswerAppraisingType taskAnswerAppraisingType;

    private Boolean canSeeAppraiser;

    private Boolean canSeeAppraised;
}
