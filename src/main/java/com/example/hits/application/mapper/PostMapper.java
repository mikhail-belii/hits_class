package com.example.hits.application.mapper;

import com.example.hits.domain.entity.post.Post;
import com.example.hits.infrastructure.persistence.entity.FileEntity;
import com.example.hits.infrastructure.persistence.entity.PostCommentEntity;
import com.example.hits.infrastructure.persistence.entity.PostEntity;
import com.example.hits.presentation.dto.file.FileModel;
import com.example.hits.presentation.dto.post.PostFullModel;
import com.example.hits.presentation.dto.post.PostShortModel;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerEntity;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

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
                .setMaxScore(postEntity.getMaxScore());
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
                .setComments(postEntity.getComments() == null ?
                        List.of() :
                        postEntity.getComments().stream()
                                .map(c -> c.toModel())
                                .toList());
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
                .setComments(postEntity.getComments() == null ?
                        List.of() :
                        postEntity.getComments().stream()
                                .map(c -> c.toModel())
                                .toList())
                .setTaskAnswer(taskAnswerEntity != null ? taskAnswerEntity.toModel() : null);
    }
}
