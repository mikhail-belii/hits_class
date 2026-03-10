package com.example.hits.domain.service.taskanswer;

import com.example.hits.application.repository.TaskAnswerRepository;
import com.example.hits.domain.entity.course.Course;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.user.User;
import com.example.hits.domain.entity.usercourse.UserCourse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class TaskAnswerService {

    private final TaskAnswerRepository taskAnswerRepository;

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

    public void createTaskAnswerForUser(Post post, User user) {
        TaskAnswer newUserTaskAnswer = createTaskAnswerForDefiniteUser(post, user);

        taskAnswerRepository.save(newUserTaskAnswer);
    }

    private TaskAnswer createTaskAnswerForDefiniteUser(Post post, User user) {
        return new TaskAnswer()
                .setPost(post)
                .setUser(user);
    }
}
