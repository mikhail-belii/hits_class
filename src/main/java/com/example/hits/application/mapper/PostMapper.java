package com.example.hits.application.mapper;

import com.example.hits.domain.aggregate.ScoredMarkCriteria;
import com.example.hits.domain.aggregate.ScoredPost;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.infrastructure.persistence.entity.*;
import com.example.hits.presentation.dto.file.FileModel;
import com.example.hits.presentation.dto.post.PostFullModel;
import com.example.hits.presentation.dto.post.PostShortModel;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
@ExtensionMethod({SimpleUserMapper.class, PostCommentMapper.class, TaskAnswerMapper.class})
public class PostMapper {

    public Post toDomain(PostEntity postEntity) {
        return new Post()
                .setId(postEntity.getId())
                .setText(postEntity.getText())
                .setUpdatedAt(postEntity.getUpdatedAt())
                .setCourseId(postEntity.getCourseEntity() != null ? postEntity.getCourseEntity().getId() : null)
                .setAuthorId(postEntity.getAuthor() != null ? postEntity.getAuthor().getId() : null)
                .setFileEntityIds(postEntity.getFileEntities().stream().map(FileEntity::getId).toList())
                .setCommentIds(postEntity.getComments().stream().map(PostCommentEntity::getId).toList())
                .setPostType(postEntity.getPostType())
                .setCreatedAt(postEntity.getCreatedAt())
                .setDeadline(postEntity.getDeadline())
                .setMinScore(postEntity.getTaskMarkEvaluationType().isAnswerScoreIsPassFail() ? 0 : postEntity.getMinScore())
                .setMaxScore(postEntity.getTaskMarkEvaluationType().isAnswerScoreIsPassFail() ? 1 : postEntity.getMaxScore())
                .setMultiplier(postEntity.getMultiplier())
                .setEvaluationFunction(postEntity.getEvaluationFunction())
                .setTaskMarkEvaluationType(postEntity.getTaskMarkEvaluationType())
                .setPassThreshold(postEntity.getPassThreshold())
                .setAppraiserDeadline(postEntity.getAppraiserDeadline())
                .setTaskAnswerAppraisingType(postEntity.getTaskAnswerAppraisingType())
                .setCanSeeAppraiser(postEntity.getCanSeeAppraiser())
                .setCanSeeAppraised(postEntity.getCanSeeAppraised());
    }

    public PostShortModel toModel(PostEntity postEntity) {
        return new PostShortModel()
                .setId(postEntity.getId())
                .setText(postEntity.getText())
                .setAuthor(postEntity.getAuthor() != null ? postEntity.getAuthor().toModel() : null)
                .setFiles(postEntity.getFileEntities() != null ?
                        postEntity.getFileEntities().stream().map(file -> new FileModel(file.getId(), file.getOriginalName())).toList() :
                        new ArrayList<>())
                .setPostType(postEntity.getPostType())
                .setCreatedAt(postEntity.getCreatedAt())
                .setDeadline(postEntity.getDeadline())
                .setMaxScore(postEntity.getMaxScore())
                .setTaskMarkEvaluationType(postEntity.getTaskMarkEvaluationType())
                .setComments(postEntity.getComments() == null ?
                        List.of() :
                        postEntity.getComments().stream()
                                .map(c -> c.toModel())
                                .toList())
                .setAppraiserDeadline(postEntity.getAppraiserDeadline())
                .setTaskAnswerAppraisingType(postEntity.getTaskAnswerAppraisingType())
                .setCanSeeAppraiser(postEntity.getCanSeeAppraiser())
                .setCanSeeAppraised(postEntity.getCanSeeAppraised());
    }

    public PostFullModel toModel(PostEntity postEntity, TaskAnswerEntity taskAnswerEntity) {
        return new PostFullModel()
                .setId(postEntity.getId())
                .setText(postEntity.getText())
                .setAuthor(postEntity.getAuthor() != null ? postEntity.getAuthor().toModel() : null)
                .setFiles(postEntity.getFileEntities() != null ?
                        postEntity.getFileEntities().stream().map(file -> new FileModel(file.getId(), file.getOriginalName())).toList() :
                        new ArrayList<>())
                .setPostType(postEntity.getPostType())
                .setCreatedAt(postEntity.getCreatedAt())
                .setDeadline(postEntity.getDeadline())
                .setMaxScore(postEntity.getMaxScore())
                .setMinScore(postEntity.getMinScore())
                .setMultiplier(postEntity.getMultiplier())
                .setEvaluationFunction(postEntity.getEvaluationFunction())
                .setTaskMarkEvaluationType(postEntity.getTaskMarkEvaluationType())
                .setComments(postEntity.getComments() == null ?
                        List.of() :
                        postEntity.getComments().stream()
                                .map(c -> c.toModel())
                                .toList())
                .setTaskAnswer(taskAnswerEntity != null ? taskAnswerEntity.toModel() : null)
                .setAppraiserDeadline(postEntity.getAppraiserDeadline())
                .setTaskAnswerAppraisingType(postEntity.getTaskAnswerAppraisingType())
                .setCanSeeAppraiser(postEntity.getCanSeeAppraiser())
                .setCanSeeAppraised(postEntity.getCanSeeAppraised());
    }

    public ScoredPost toDomain(@NotNull PostEntity postEntity, TaskAnswerEntity taskAnswerEntity) {
        Float minScore = postEntity.getTaskMarkEvaluationType().isAnswerScoreIsPassFail() ? 0 : postEntity.getMinScore();
        return new ScoredPost(
                postEntity.getPostType(),
                postEntity.getTaskMarkEvaluationType(),
                postEntity.getMultiplier(),
                postEntity.getEvaluationFunction(),
                taskAnswerEntity != null ? taskAnswerEntity.getScore() : minScore,
                taskAnswerEntity != null ? taskAnswerEntity.getTeacherScore() : null,
                minScore,
                postEntity.getTaskMarkEvaluationType().isAnswerScoreIsPassFail() ? 1 : postEntity.getMaxScore());
    }
}
