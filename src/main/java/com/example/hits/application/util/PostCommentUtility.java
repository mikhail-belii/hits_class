package com.example.hits.application.util;

import com.example.hits.infrastructure.persistence.entity.PostCommentEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public class PostCommentUtility {

    public boolean isCommentAvailableForEditing(PostCommentEntity postComment, UserEntity user) {
        return Objects.equals(user, postComment.getAuthor());
    }

}
