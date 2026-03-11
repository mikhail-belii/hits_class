package com.example.hits.domain.service.taskanswer;

import com.example.hits.application.model.taskanswer.TaskAnswerFullModel;
import com.example.hits.application.model.taskanswer.TaskAnswerModel;
import com.example.hits.application.repository.PostRepository;
import com.example.hits.application.repository.TaskAnswerRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.application.util.PostUtility;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.post.PostType;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.taskanswer.TaskAnswerStatus;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.user.UserCourseRole;
import com.example.hits.domain.entity.usercourse.UserCourse;
import com.example.hits.domain.mapper.TaskAnswerMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskAnswerGeneralService {

    private final TaskAnswerRepository taskAnswerRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    Map<TaskAnswerStatus, Integer> priority = Map.of(
            TaskAnswerStatus.NEW, 1,
            TaskAnswerStatus.NOT_COMPLETED, 2,
            TaskAnswerStatus.COMPLETED, 3,
            TaskAnswerStatus.COMPETED_AFTER_DEADLINE, 4
    );

    @Transactional
    public void createTaskAnswerForEveryCourseMember(Course course, Post post) {
        if (course.getCourseUsers() == null || course.getCourseUsers().isEmpty()) {
            return;
        }

        var taskAnswers = new ArrayList<TaskAnswer>(course.getCourseUsers().size());
        for (UserCourse userCourse : course.getCourseUsers()) {
            User user = userCourse.getUser();

            taskAnswers.add(createTaskAnswerForDefiniteUser(post, user));
        }

        taskAnswerRepository.saveAll(taskAnswers);
    }

    public List<TaskAnswerModel> getAllUserTaskAnswers(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);

        return formAllUserTaskAnswers(user).stream()
                .sorted(
                        Comparator
                                .comparing((TaskAnswerModel a) -> priority.get(a.getStatus()))
                                .thenComparing(TaskAnswerModel::getPostName))
                .toList();
    }

    public List<TaskAnswerFullModel> getAllPostTaskAnswers(UUID postId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
        Post post = postRepository.findById(postId)
                .orElseThrow(ExceptionUtility::postNotFoundException);

        if (post.getCourse() == null || !PostUtility.isAvailableForEditing(post.getCourse(), user)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        if (post.getPostType() != PostType.TASK) {
            throw ExceptionUtility.badRequestException("Post is not a task type");
        }

        return taskAnswerRepository.findAllByPostId(postId).stream()
                .filter(taskAnswerModel -> taskAnswerModel.getSubmittedAt() != null)
                .map(TaskAnswerMapper::toFullModel)
                .sorted(Comparator
                        .comparing((TaskAnswerFullModel model) -> model.getUser().getFirstName(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(model -> model.getUser().getLastName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public TaskAnswerModel getUserPostTaskAnswer(UUID postId, UUID userId) {
        var taskAnswer = taskAnswerRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(ExceptionUtility::taskAnswerNotFoundException);

        return TaskAnswerMapper.toModel(taskAnswer);
    }

    public void createTaskAnswersForNewCourseUser(User user, Course course) {
        List<Post> coursePosts = postRepository.findAllByCourseAndPostType(course, PostType.TASK);

        for (Post post : coursePosts) {
            createTaskAnswerForUser(post, user);
        }
    }

    public void createTaskAnswerForUser(Post post, User user) {
        TaskAnswer newUserTaskAnswer = createTaskAnswerForDefiniteUser(post, user);

        taskAnswerRepository.save(newUserTaskAnswer);
    }

    private List<TaskAnswerModel> formAllUserTaskAnswers(User user) {
        List<TaskAnswerModel> userTaskAnswers = new ArrayList<>();

        for (UserCourse userCourse : user.getUserCourses()) {
            if (userCourse.getUserRole() == UserCourseRole.STUDENT) {
                userTaskAnswers.addAll(getAllUserCourseTaskAnswer(user, userCourse.getCourse()));
            }
        }

        return userTaskAnswers;
    }

    private List<TaskAnswerModel> getAllUserCourseTaskAnswer(User user, Course course) {
        return taskAnswerRepository.findAllByUserIdAndPostCourseId(user.getId(), course.getId()).stream()
                .map(TaskAnswerMapper::toModel)
                .toList();
    }

    private TaskAnswer createTaskAnswerForDefiniteUser(Post post, User user) {
        return new TaskAnswer()
                .setPost(post)
                .setUser(user);
    }
}
