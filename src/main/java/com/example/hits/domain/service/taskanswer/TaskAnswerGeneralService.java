package com.example.hits.domain.service.taskanswer;

import com.example.hits.application.model.taskanswer.TaskAnswerModel;
import com.example.hits.application.repository.TaskAnswerRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
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

        return formAllUserTaskAnswers(user).stream().sorted(
                        Comparator
                                .comparing((TaskAnswerModel a) -> priority.get(a.getStatus()))
                                .thenComparing(TaskAnswerModel::getPostName))
                .toList();
    }

    public void createTaskAnswerForUser(Post post, User user) {
        TaskAnswer newUserTaskAnswer = createTaskAnswerForDefiniteUser(post, user);

        taskAnswerRepository.save(newUserTaskAnswer);
    }

    public List<TaskAnswerModel> getAllPostTaskAnswers(UUID postId, UUID userId) {
        return new ArrayList<>();
    }

    public TaskAnswerModel getUserPostTaskAnswer(UUID postId, UUID userId) {
        return null;
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
