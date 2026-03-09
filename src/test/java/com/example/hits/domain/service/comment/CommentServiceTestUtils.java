package com.example.hits.domain.service.comment;

import com.example.hits.application.model.comment.postcomment.PostCommentCreateModel;
import com.example.hits.application.model.comment.postcomment.PostCommentEditModel;
import com.example.hits.application.model.course.CourseCreateModel;
import com.example.hits.application.model.course.CourseEditModel;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.postcomment.PostComment;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;

import java.util.UUID;

public class CommentServiceTestUtils {

    public static User createUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        return user;
    }

    public static Course createCourse(String name, String description, Boolean isArchived) {
        Course course = new Course();
        course.setId(UUID.randomUUID());
        course.setName(name);
        course.setDescription(description);
        course.setIsArchived(isArchived);
        return course;
    }

    public static UserCourse createUserCourse(User user, Course course, UserCourseRole userCourseRole) {
        UserCourse userCourse = new UserCourse();
        userCourse.setId(UUID.randomUUID());
        userCourse.setUser(user);
        userCourse.setCourse(course);
        userCourse.setUserRole(userCourseRole);
        return userCourse;
    }

    public static PostComment createPostComment(User user, Post post) {
        PostComment postComment = new PostComment();
        postComment.setId(UUID.randomUUID());
        postComment.setAuthor(user);
        postComment.setPost(post);
        return postComment;
    }

    public static Post createPost(Course course, User user) {
        Post post = new Post();
        post.setId(UUID.randomUUID());
        post.setAuthor(user);
        post.setCourse(course);
        return post;
    }

    public static PostCommentCreateModel createPostCommentCreateModel(String text) {
        PostCommentCreateModel postCommentCreateModel = new PostCommentCreateModel();
        postCommentCreateModel.setText(text);
        return postCommentCreateModel;
    }

    public static PostCommentEditModel createPostCommentEditModel(String text) {
        PostCommentEditModel postCommentEditModel = new PostCommentEditModel();
        postCommentEditModel.setText(text);
        return postCommentEditModel;
    }

}
