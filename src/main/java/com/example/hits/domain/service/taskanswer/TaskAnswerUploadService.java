package com.example.hits.domain.service.taskanswer;

import com.example.hits.application.model.attachment.AttachmentModel;
import com.example.hits.application.model.taskanswer.TaskRateRequestModel;
import com.example.hits.application.repository.TaskAnswerRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.application.util.PostUtility;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskAnswerUploadService {

    private final TaskAnswerRepository taskAnswerRepository;
    private final UserRepository userRepository;

    public void evaluateTask(UUID taskAnswerId, TaskRateRequestModel taskRate, UUID userId) {
        TaskAnswer taskAnswer = getTaskAnswer(taskAnswerId);
        User user = getUser(userId);

        Post post = taskAnswer.getPost();

        if (post.getCourse() == null || !PostUtility.isAvailableForEditing(post.getCourse(), user)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        if (post.getMaxScore() < taskRate.getRate()) {
            throw ExceptionUtility.badRequestException("Invalid score");
        }

        taskAnswer.setScore(taskRate.getRate());

        taskAnswerRepository.save(taskAnswer);
    }

    public void appendFiles(UUID taskAnswerId, List<AttachmentModel> attachmentModels, UUID userId) {

    }

    public void unpinFiles(UUID taskAnswerId, UUID fileId, UUID userId) {

    }

    public void submitTask(UUID taskAnswerId, UUID userId) {

    }

    public void unsubmitTask(UUID taskAnswerId, UUID userId) {

    }

    private TaskAnswer getTaskAnswer(UUID taskAnswerId) {
        return taskAnswerRepository.findById(taskAnswerId)
                .orElseThrow(ExceptionUtility::taskAnswerNotFoundException);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
    }
}
