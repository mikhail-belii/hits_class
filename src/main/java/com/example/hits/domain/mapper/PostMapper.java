package com.example.hits.domain.mapper;

import com.example.hits.application.model.attachment.AttachmentModel;
import com.example.hits.application.model.post.PostShortModel;
import com.example.hits.domain.entity.post.Post;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
@ExtensionMethod({SimpleUserMapper.class, PostCommentMapper.class})
public class PostMapper {

    public PostShortModel toModel(Post postEntity) {
        return new PostShortModel()
                .setId(postEntity.getId())
                .setText(postEntity.getText())
                .setAuthor(postEntity.getAuthor() != null ? postEntity.getAuthor().toModel() : null)
                .setAttachments(postEntity.getAttachments() != null ?
                        postEntity.getAttachments().stream().map(attachment -> new AttachmentModel(attachment.getId())).toList() :
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
}
