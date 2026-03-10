package com.example.hits.application.controller;

import com.example.hits.application.model.attachment.AttachmentModel;
import com.example.hits.application.model.taskanswer.TaskAnswerModel;
import com.example.hits.application.model.taskanswer.TaskRateRequestModel;
import com.example.hits.domain.service.taskanswer.TaskAnswerGeneralService;
import com.example.hits.domain.service.taskanswer.TaskAnswerUploadService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/task-answer")
@RequiredArgsConstructor
public class TaskAnswerController {

    private final TaskAnswerGeneralService taskAnswerGeneralService;
    private final TaskAnswerUploadService taskAnswerUploadService;

    @GetMapping("/all")
    public List<TaskAnswerModel> getAllUserTaskAnswers(@RequestAttribute("userId") UUID userId) {
        return taskAnswerGeneralService.getAllUserTaskAnswers(userId);
    }

    @GetMapping("/post/{postId}/all")
    @Operation(summary = "Get all post task answers [FOR TEACHER+]")
    public List<TaskAnswerModel> getAllPostTaskAnswers(@PathVariable UUID postId, @RequestAttribute("userId") UUID userId) {
        return taskAnswerGeneralService.getAllPostTaskAnswers(postId, userId);
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "Get post task answer [FOR STUDENT]")
    public TaskAnswerModel getUserPostTaskAnswer(@PathVariable UUID postId, @RequestAttribute("userId") UUID userId) {
        return taskAnswerGeneralService.getUserPostTaskAnswer(postId, userId);
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

    @PutMapping("/task-answer/{taskAnswerId}/evaluate")
    public void evaluateTask(@PathVariable UUID taskAnswerId,
                             @RequestBody TaskRateRequestModel taskRate,
                             @RequestAttribute("userId") UUID userId) {

    }
}
