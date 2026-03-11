package com.example.hits.domain.mapper;

import com.example.hits.application.model.file.FileModel;
import com.example.hits.application.model.post.PostFullModel;
import com.example.hits.application.model.post.PostShortModel;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
@ExtensionMethod({SimpleUserMapper.class, PostCommentMapper.class, TaskAnswerMapper.class})
public class PostMapper {

    public PostShortModel toModel(Post postEntity) {
        return new PostShortModel()
                .setId(postEntity.getId())
                .setText(postEntity.getText())
                .setAuthor(postEntity.getAuthor() != null ? postEntity.getAuthor().toModel() : null)
                .setFiles(postEntity.getFiles() != null ?
                        postEntity.getFiles().stream().map(file -> new FileModel(file.getId(), file.getOriginalName())).toList() :
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

    public PostFullModel toModel(Post postEntity, TaskAnswer taskAnswer) {
        return new PostFullModel()
                .setId(postEntity.getId())
                .setText(postEntity.getText())
                .setAuthor(postEntity.getAuthor() != null ? postEntity.getAuthor().toModel() : null)
                .setFiles(postEntity.getFiles() != null ?
                        postEntity.getFiles().stream().map(file -> new FileModel(file.getId(), file.getOriginalName())).toList() :
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
                .setTaskAnswer(taskAnswer != null ? taskAnswer.toModel() : null);
    }
}
