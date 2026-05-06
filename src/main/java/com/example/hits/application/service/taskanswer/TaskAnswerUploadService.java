package com.example.hits.application.service.taskanswer;

import com.example.hits.domain.aggregate.TaskEvaluationAggregate;
import com.example.hits.infrastructure.persistence.entity.FileEntity;
import com.example.hits.infrastructure.persistence.entity.PostEntity;
import com.example.hits.infrastructure.persistence.entity.UserEntity;
import com.example.hits.infrastructure.persistence.repository.TaskAnswerRepositoryImpl;
import com.example.hits.presentation.dto.file.FileModel;
import com.example.hits.presentation.request.taskanswer.TaskRateRequestModel;
import com.example.hits.infrastructure.persistence.repository.FileRepository;
import com.example.hits.infrastructure.persistence.repository.TaskAnswerRepository;
import com.example.hits.infrastructure.persistence.repository.UserRepository;
import com.example.hits.application.util.ExceptionUtility;
import com.example.hits.application.util.PostUtility;
import com.example.hits.infrastructure.persistence.entity.TaskAnswerEntity;
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
    private final TaskAnswerRepositoryImpl taskAnswerRepositoryImpl;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;

    public void evaluateTask(UUID taskAnswerId, TaskRateRequestModel taskScore, UUID userId) {
        TaskEvaluationAggregate taskEvaluationAggregate = taskAnswerRepositoryImpl.getTaskEvaluationAggregate(taskAnswerId, userId);

        taskEvaluationAggregate.evaluateTask(taskScore.getRate());

        taskAnswerRepositoryImpl.saveTaskEvaluationAggregate(taskEvaluationAggregate);
    }

    public void appendFiles(UUID taskAnswerId, List<FileModel> fileModels, UUID userId) {
        TaskAnswerEntity taskAnswerEntity = getTaskAnswer(taskAnswerId);
        UserEntity userEntity = getUser(userId);

        if (!taskAnswerEntity.getUserEntity().equals(userEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        if (taskAnswerEntity.getSubmittedAt() != null) {
            throw ExceptionUtility.badRequestException("Task already submitted");
        }

        taskAnswerEntity.setFileEntities(formFiles(taskAnswerEntity, fileModels.stream()
                        .map(FileModel::getId)
                        .toList()));

        taskAnswerRepository.save(taskAnswerEntity);
    }

    public void unpinFiles(UUID taskAnswerId, UUID fileId, UUID userId) {
        TaskAnswerEntity taskAnswerEntity = getTaskAnswer(taskAnswerId);
        UserEntity userEntity = getUser(userId);

        if (!taskAnswerEntity.getUserEntity().equals(userEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        if (taskAnswerEntity.getSubmittedAt() != null) {
            throw ExceptionUtility.badRequestException("Task already submitted");
        }

        boolean removed = taskAnswerEntity.getFileEntities().removeIf(file -> {
                    boolean shouldRemove = fileId.equals(file.getId());
                    if (shouldRemove) {
                        file.setTaskAnswerEntity(null);
                    }
                    return shouldRemove;
                }
        );

        if (!removed) {
            throw ExceptionUtility.badRequestException("File not found in attachments");
        }

        taskAnswerRepository.save(taskAnswerEntity);
    }

    public void submitTask(UUID taskAnswerId, UUID userId) {
        TaskAnswerEntity taskAnswerEntity = getTaskAnswer(taskAnswerId);
        UserEntity userEntity = getUser(userId);

        if (!taskAnswerEntity.getUserEntity().equals(userEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        taskAnswerEntity.setSubmittedAt(LocalDateTime.now());

        taskAnswerRepository.save(taskAnswerEntity);
    }

    public void unsubmitTask(UUID taskAnswerId, UUID userId) {
        TaskAnswerEntity taskAnswerEntity = getTaskAnswer(taskAnswerId);
        UserEntity userEntity = getUser(userId);

        if (!taskAnswerEntity.getUserEntity().equals(userEntity)) {
            throw ExceptionUtility.forbiddenRightsException();
        }

        if (taskAnswerEntity.getScore() != 0) {
            throw ExceptionUtility.badRequestException("Task already evaluated");
        }

        taskAnswerEntity.setSubmittedAt(null);

        taskAnswerRepository.save(taskAnswerEntity);
    }

    private TaskAnswerEntity getTaskAnswer(UUID taskAnswerId) {
        return taskAnswerRepository.findById(taskAnswerId)
                .orElseThrow(ExceptionUtility::taskAnswerNotFoundException);
    }

    private UserEntity getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(ExceptionUtility::userNotFoundException);
    }

    private List<FileEntity> formFiles(TaskAnswerEntity taskAnswerEntity, List<UUID> fileIds) {
        var files = fileRepository.findAllById(fileIds);

        if (files.size() != fileIds.size()) {
            throw ExceptionUtility.badRequestException("One or more files not found");
        }

        var filesById = files.stream()
                .collect(Collectors.toMap(FileEntity::getId, Function.identity()));

        if (taskAnswerEntity.getFileEntities() != null) {
            taskAnswerEntity.getFileEntities().forEach(file -> file.setTaskAnswerEntity(null));
        }

        List<FileEntity> newFileEntities = new ArrayList<>();

        for (UUID fileId : fileIds) {
            var file = filesById.get(fileId);
            if (file == null) {
                throw ExceptionUtility.badRequestException("One or more files not found");
            }

            if (file.getPostEntity() != null || (file.getTaskAnswerEntity() != null && !file.getTaskAnswerEntity().equals(taskAnswerEntity))) {
                throw ExceptionUtility.badRequestException("File is already attached");
            }

            file.setPostEntity(null);
            file.setTaskAnswerEntity(taskAnswerEntity);
            newFileEntities.add(file);
        }

        return newFileEntities;
    }
}
