package com.example.hits.application.util;

import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.postcomment.PostComment;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.taskanswercomment.TaskAnswerComment;
import com.example.hits.domain.entity.user.User;
import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public class PostCommentUtility {

    public boolean isCommentAvailableForEditing(PostComment postComment, User user) {
        return Objects.equals(user, postComment.getAuthor());
    }

}
