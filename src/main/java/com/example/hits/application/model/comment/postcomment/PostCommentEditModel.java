package com.example.hits.application.model.comment.postcomment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain=true)
public class PostCommentEditModel {

    private String text;

}
