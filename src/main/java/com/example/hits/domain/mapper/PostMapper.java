package com.example.hits.domain.mapper;

import com.example.hits.application.model.file.FileModel;
import com.example.hits.application.model.post.PostModel;
import com.example.hits.domain.entity.post.Post;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
@ExtensionMethod({SimpleUserMapper.class, PostCommentMapper.class})
public class PostMapper {

    public PostModel toModel(Post postEntity) {
        return new PostModel()
                .setId(postEntity.getId())
                .setText(postEntity.getText())
                .setAuthor(postEntity.getAuthor() != null ? postEntity.getAuthor().toModel() : null)
                .setFiles(postEntity.getFiles() != null ?
                        postEntity.getFiles().stream().map(file -> new FileModel(file.getId())).toList() :
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
