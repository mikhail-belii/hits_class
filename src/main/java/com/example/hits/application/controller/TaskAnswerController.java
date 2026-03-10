package com.example.hits.application.controller;

import com.example.hits.application.model.attachment.AttachmentModel;
import com.example.hits.application.model.taskanswer.TaskAnswerModel;
import com.example.hits.domain.service.taskanswer.TaskAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/task-answer")
@RequiredArgsConstructor
public class TaskAnswerController {

    private final TaskAnswerService taskAnswerService;

    @GetMapping("/all")
    public List<TaskAnswerModel> getAllUserTaskAnswers(@RequestAttribute("userId") UUID userId) {
        // todo: только для курсов, в которых пользователь является студентом
        return new ArrayList<>();
    }

    @PostMapping("/pin-file/{taskAnswerId}")
    public void appendFiles(@PathVariable UUID taskAnswerId,
                            @RequestBody List<AttachmentModel> attachmentModels,
                            @RequestAttribute("userId") UUID userId) {

    }

    @DeleteMapping("/unpin-file/{taskAnswerId}/file/{fileId}")
    public void unpinFile(@PathVariable UUID taskAnswerId,
                          @PathVariable UUID fileId,
                          @RequestAttribute("userId") UUID userId) {

    }

    @PostMapping("/submit/{taskAnswerId}")
    public void submitTask(@PathVariable UUID taskAnswerId, @RequestAttribute("userId") UUID userId) {

    }

    @DeleteMapping("/submit/{taskAnswerId}")
    public void unsubmitTask(@PathVariable UUID taskAnswerId, @RequestAttribute("userId") UUID userId) {

    }

    @GetMapping("/post/{postId}")
    public List<TaskAnswerModel> getAllPostTaskAnswers(@PathVariable UUID postId, @RequestAttribute("userId") UUID userId) {
        return new ArrayList<>();
    }
}
