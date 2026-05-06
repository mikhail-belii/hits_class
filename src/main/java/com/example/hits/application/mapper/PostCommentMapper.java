package com.example.hits.application.mapper;

import com.example.hits.infrastructure.persistence.entity.PostCommentEntity;
import com.example.hits.presentation.dto.comment.postcomment.PostCommentModel;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

@UtilityClass
@ExtensionMethod(SimpleUserMapper.class)
public class PostCommentMapper {

    public PostCommentModel toModel(PostCommentEntity postCommentEntity) {
        return new PostCommentModel()
                .setId(postCommentEntity.getId())
                .setText(postCommentEntity.getText())
                .setAuthor(postCommentEntity.getAuthor() != null
                        ? postCommentEntity.getAuthor().toModel()
                        : null)
                .setCreatedAt(postCommentEntity.getCreatedAt())
                .setUpdatedAt(postCommentEntity.getUpdatedAt());
    }
}
