package com.example.hits.application.model.comment.taskanswercomment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain=true)
public class TaskAnswerCommentEditModel {

    private String text;

}
