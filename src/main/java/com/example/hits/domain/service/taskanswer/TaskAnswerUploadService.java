package com.example.hits.domain.service.taskanswer;

import com.example.hits.application.model.attachment.AttachmentModel;
import com.example.hits.application.model.taskanswer.TaskRateRequestModel;
import com.example.hits.application.repository.TaskAnswerRepository;
import com.example.hits.application.repository.UserRepository;
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

    }

    public void appendFiles(UUID taskAnswerId, List<AttachmentModel> attachmentModels, UUID userId) {

    }

    public void unpinFiles(UUID taskAnswerId, UUID fileId, UUID userId) {

    }

    public void submitTask(UUID taskAnswerId, UUID userId) {

    }

    public void unsubmitTask(UUID taskAnswerId, UUID userId) {

    }
}
