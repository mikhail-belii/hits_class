package com.example.hits.domain.mapper;

import com.example.hits.application.model.comment.postcomment.PostCommentModel;
import com.example.hits.domain.entity.postcomment.PostComment;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

@UtilityClass
@ExtensionMethod(SimpleUserMapper.class)
public class PostCommentMapper {

    public PostCommentModel toModel(PostComment postCommentEntity) {
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
