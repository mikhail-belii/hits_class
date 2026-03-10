package com.example.hits.domain.service.taskanswer;

import com.example.hits.application.model.attachment.AttachmentModel;
import com.example.hits.application.model.taskanswer.TaskRateRequestModel;
import com.example.hits.application.repository.FileRepository;
import com.example.hits.application.repository.TaskAnswerRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.application.util.PostUtility;
import com.example.hits.domain.entity.attachment.Attachment;
import com.example.hits.domain.entity.file.File;
import com.example.hits.domain.entity.post.Post;
import com.example.hits.domain.entity.taskanswer.TaskAnswer;
import com.example.hits.domain.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskAnswerUploadService {

    private final TaskAnswerRepository taskAnswerRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;

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
        TaskAnswer taskAnswer = getTaskAnswer(taskAnswerId);
        User user = getUser(userId);

        if (!taskAnswer.getUser().equals(user)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        if (taskAnswer.getSubmittedAt() != null) {
            throw ExceptionUtility.badRequestException("Task already submitted");
        }

        taskAnswer.setAttachments(formAttachments(taskAnswer, attachmentModels.stream()
                        .map(AttachmentModel::getId)
                        .toList()));

        taskAnswerRepository.save(taskAnswer);
    }

    public void unpinFiles(UUID taskAnswerId, UUID fileId, UUID userId) {
        TaskAnswer taskAnswer = getTaskAnswer(taskAnswerId);
        User user = getUser(userId);

        if (!taskAnswer.getUser().equals(user)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        if (taskAnswer.getSubmittedAt() != null) {
            throw ExceptionUtility.badRequestException("Task already submitted");
        }

        boolean removed = taskAnswer.getAttachments().removeIf(attachment ->
                attachment.getFile() != null && fileId.equals(attachment.getFile().getId())
        );

        if (!removed) {
            throw ExceptionUtility.badRequestException("File not found in attachments");
        }

        taskAnswerRepository.save(taskAnswer);
    }

    public void submitTask(UUID taskAnswerId, UUID userId) {
        TaskAnswer taskAnswer = getTaskAnswer(taskAnswerId);
        User user = getUser(userId);

        if (!taskAnswer.getUser().equals(user)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        taskAnswer.setSubmittedAt(LocalDateTime.now());

        taskAnswerRepository.save(taskAnswer);
    }

    public void unsubmitTask(UUID taskAnswerId, UUID userId) {
        TaskAnswer taskAnswer = getTaskAnswer(taskAnswerId);
        User user = getUser(userId);

        if (!taskAnswer.getUser().equals(user)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        taskAnswer.setSubmittedAt(null);

        taskAnswerRepository.save(taskAnswer);
    }

    private TaskAnswer getTaskAnswer(UUID taskAnswerId) {
        return taskAnswerRepository.findById(taskAnswerId)
                .orElseThrow(ExceptionUtility::taskAnswerNotFoundException);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
    }

    private List<Attachment> formAttachments(TaskAnswer taskAnswer, List<UUID> fileIds) {
        var files = fileRepository.findAllById(fileIds);

        if (files.size() != fileIds.size()) {
            throw ExceptionUtility.badRequestException("One or more files not found");
        }

        var filesById = files.stream()
                .collect(Collectors.toMap(File::getId, Function.identity()));

        List<Attachment> attachments = new ArrayList<>();

        for (UUID fileId : fileIds) {
            var file = filesById.get(fileId);
            if (file == null) {
                throw ExceptionUtility.badRequestException("One or more files not found");
            }

            attachments.add(new Attachment()
                    .setFile(file)
                    .setTaskAnswer(taskAnswer)
                    .setCreatedAt(LocalDateTime.now()));
        }

        return attachments;
    }
}
