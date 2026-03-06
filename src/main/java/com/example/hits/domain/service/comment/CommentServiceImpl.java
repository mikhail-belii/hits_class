package com.example.hits.domain.service.comment;

import com.example.hits.application.model.comment.postcomment.PostCommentCreateModel;
import com.example.hits.application.model.comment.postcomment.PostCommentEditModel;
import com.example.hits.application.model.comment.postcomment.PostCommentModel;
import com.example.hits.application.model.comment.taskanswercomment.TaskAnswerCommentCreateModel;
import com.example.hits.application.model.comment.taskanswercomment.TaskAnswerCommentEditModel;
import com.example.hits.application.model.comment.taskanswercomment.TaskAnswerCommentModel;
import com.example.hits.application.model.course.*;
import com.example.hits.application.repository.CourseRepository;
import com.example.hits.application.repository.UserCourseRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.application.service.CommentService;
import com.example.hits.application.service.CourseService;
import com.example.hits.application.util.CourseUtility;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import com.example.hits.domain.mapper.CourseMapper;
import com.example.hits.domain.mapper.UserCourseMapper;
import com.example.hits.domain.service.course.CourseCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    public List<PostCommentModel> getPostComments(UUID requestingUserId, UUID postId) {
        return null;
    }

    public PostCommentModel createPostComment(
            UUID requestingUserId,
            UUID postId,
            PostCommentCreateModel postCommentCreateModel) {
        return null;
    }

    public PostCommentModel editPostComment(
            UUID requestingUserId,
            UUID postId,
            PostCommentEditModel postCommentEditModel) {
        return null;
    }

    public List<TaskAnswerCommentModel> getTaskAnswerComments(UUID requestingUserId, UUID taskAnswerId) {
        return null;
    }

    public TaskAnswerCommentModel createTaskAnswerComment(
            UUID requestingUserId,
            UUID postId,
            TaskAnswerCommentCreateModel taskAnswerCommentCreateModel) {
        return null;
    }

    public TaskAnswerCommentModel editTaskAnswerComment(
            UUID requestingUserId,
            UUID postId,
            TaskAnswerCommentEditModel taskAnswerCommentEditModel) {
        return null;
    }

}
