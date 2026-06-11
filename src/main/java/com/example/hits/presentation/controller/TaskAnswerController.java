package com.example.hits.presentation.controller;

import com.example.hits.presentation.dto.file.FileModel;
import com.example.hits.presentation.dto.taskanswer.AvailablePeerEvaluationModel;
import com.example.hits.presentation.dto.taskanswer.TaskAnswerCriteriaScoreModel;
import com.example.hits.presentation.dto.taskanswer.TaskAnswerFullModel;
import com.example.hits.presentation.dto.taskanswer.PeerEvaluationModel;
import com.example.hits.presentation.dto.taskanswer.TaskAnswerModel;
import com.example.hits.presentation.request.taskanswer.CriteriaScoreRequest;
import com.example.hits.presentation.request.taskanswer.TaskRateRequestModel;
import com.example.hits.application.service.taskanswer.TaskAnswerGeneralService;
import com.example.hits.application.service.taskanswer.TaskAnswerUploadService;
import com.example.hits.application.service.peer.PeerEvaluationService;
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
    private final PeerEvaluationService peerEvaluationService;

    @GetMapping("/all")
    public List<TaskAnswerModel> getAllUserTaskAnswers(@RequestAttribute("userId") UUID userId) {
        return taskAnswerGeneralService.getAllUserTaskAnswers(userId);
    }

    @GetMapping("/post/{postId}/all")
    @Operation(summary = "Get all post task answers [FOR TEACHER+]")
    public List<TaskAnswerFullModel> getAllPostTaskAnswers(@PathVariable UUID postId, @RequestAttribute("userId") UUID userId) {
        return taskAnswerGeneralService.getAllPostTaskAnswers(postId, userId);
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "Get post task answer [FOR STUDENT]")
    public TaskAnswerModel getUserPostTaskAnswer(@PathVariable UUID postId, @RequestAttribute("userId") UUID userId) {
        return taskAnswerGeneralService.getUserPostTaskAnswer(postId, userId);
    }

    @PostMapping("/pin-file/{taskAnswerId}")
    public void appendFiles(@PathVariable UUID taskAnswerId,
                            @RequestBody List<FileModel> fileModels,
                            @RequestAttribute("userId") UUID userId) {
        taskAnswerUploadService.appendFiles(taskAnswerId, fileModels, userId);
    }

    @DeleteMapping("/unpin-file/{taskAnswerId}/file/{fileId}")
    public void unpinFile(@PathVariable UUID taskAnswerId,
                          @PathVariable UUID fileId,
                          @RequestAttribute("userId") UUID userId) {
        taskAnswerUploadService.unpinFiles(taskAnswerId, fileId, userId);
    }

    @PostMapping("/submit/{taskAnswerId}")
    public void submitTask(@PathVariable UUID taskAnswerId, @RequestAttribute("userId") UUID userId) {
        taskAnswerUploadService.submitTask(taskAnswerId, userId);
    }

    @DeleteMapping("/submit/{taskAnswerId}")
    public void unsubmitTask(@PathVariable UUID taskAnswerId, @RequestAttribute("userId") UUID userId) {
        taskAnswerUploadService.unsubmitTask(taskAnswerId, userId);
    }

    @GetMapping("/task-answer/{taskAnswerId}/criteria-scores")
    @Operation(summary = "Mark criteria scores for a task answer [FOR STUDENT owner or TEACHER+]")
    public List<TaskAnswerCriteriaScoreModel> getTaskAnswerCriteriaScores(@PathVariable UUID taskAnswerId,
                                                                          @RequestAttribute("userId") UUID userId) {
        return taskAnswerGeneralService.getCriteriaScoresForTaskAnswer(taskAnswerId, userId);
    }

    @PutMapping("/task-answer/{taskAnswerId}/self-assessment/criteria-scores")
    @Operation(summary = "Set self-assessment score for one mark criterion [FOR STUDENT owner, SELF_ASSESSMENT tasks only]")
    public void putSelfAssessmentCriteriaScore(@PathVariable UUID taskAnswerId,
                                               @RequestBody CriteriaScoreRequest request,
                                               @RequestAttribute("userId") UUID userId) {
        taskAnswerUploadService.putSelfAssessmentCriteriaScore(taskAnswerId, request, userId);
    }

    @PutMapping("/task-answer/{taskAnswerId}/criteria-scores")
    @Operation(summary = "Set score for one mark criterion on a task answer [FOR TEACHER+]")
    public void putCriteriaScore(@PathVariable UUID taskAnswerId,
                                 @RequestBody CriteriaScoreRequest request,
                                 @RequestAttribute("userId") UUID userId) {
        taskAnswerUploadService.putCriteriaScore(taskAnswerId, request, userId);
    }

    @PutMapping("/task-answer/{taskAnswerId}/evaluate")
    public void evaluateTask(@PathVariable UUID taskAnswerId,
                             @RequestBody TaskRateRequestModel taskRate,
                             @RequestAttribute("userId") UUID userId) {
        taskAnswerUploadService.evaluateTaskManually(taskAnswerId, taskRate, userId);
    }

    @GetMapping("/to-appraise")
    @Operation(summary = "Get task answers to appraise [FOR STUDENT]")
    public List<PeerEvaluationModel> getTasksToAppraise(@RequestAttribute("userId") UUID userId,
                                                           @RequestParam(required = false) UUID postId) {
        return taskAnswerGeneralService.getTasksToAppraise(userId, postId);
    }

    @GetMapping("/post/{postId}/available-to-appraise")
    @Operation(summary = "Get all task answers availability for self-selected peer evaluation [FOR STUDENT]")
    public List<AvailablePeerEvaluationModel> getAvailableWorksToAppraise(@PathVariable UUID postId,
                                                                          @RequestAttribute("userId") UUID userId) {
        return taskAnswerGeneralService.getAvailableWorksToAppraise(postId, userId);
    }

    @PostMapping("/task-answer/{taskAnswerId}/select-to-appraise")
    @Operation(summary = "Select task answer for self-selected peer evaluation [FOR STUDENT]")
    public void selectWorkToAppraise(@PathVariable UUID taskAnswerId,
                                     @RequestAttribute("userId") UUID userId) {
        taskAnswerGeneralService.selectWorkToAppraise(taskAnswerId, userId);
    }

    @GetMapping("/peer-evaluation/{evaluationId}")
    @Operation(summary = "Get peer evaluation detail [FOR STUDENT appraiser]")
    public PeerEvaluationModel getPeerEvaluationDetail(@PathVariable UUID evaluationId,
                                                        @RequestAttribute("userId") UUID userId) {
        return taskAnswerGeneralService.getPeerEvaluationDetail(evaluationId, userId);
    }

    @PutMapping("/appraiser/{appraiserId}/criteria-scores")
    @Operation(summary = "Set criteria scores as appraiser [FOR STUDENT appraiser]")
    public void putAppraiserCriteriaScores(@PathVariable UUID appraiserId,
                                            @RequestBody List<CriteriaScoreRequest> requests,
                                            @RequestAttribute("userId") UUID userId) {
        peerEvaluationService.submitAppraiserScore(appraiserId, requests, userId);
    }

    @PostMapping("/appraiser/{appraiserId}/evaluate")
    @Operation(summary = "Evaluate task answer by student [FOR STUDENT appraiser]")
    public void submitAppraiserEvaluation(@PathVariable UUID appraiserId,
                                          @RequestBody TaskRateRequestModel taskRate,
                                           @RequestAttribute("userId") UUID userId) {
        peerEvaluationService.evaluateAppraiser(appraiserId, taskRate, userId);
    }

    @PutMapping("/appraiser/{appraiserId}/override")
    @Operation(summary = "Override peer evaluation score [FOR TEACHER+]")
    public void overrideAppraiserEvaluation(@PathVariable UUID appraiserId,
                                            @RequestBody TaskRateRequestModel taskRate,
                                            @RequestAttribute("userId") UUID userId) {
        peerEvaluationService.overrideAppraiserScore(appraiserId, taskRate.getRate(), userId);
    }

    @GetMapping("/task-answer/{taskAnswerId}/appraisers")
    @Operation(summary = "Who evaluated my answer [FOR STUDENT owner, respects canSeeAppraiser]")
    public List<PeerEvaluationModel> getTaskAnswerAppraisers(@PathVariable UUID taskAnswerId,
                                                                @RequestAttribute("userId") UUID userId) {
        return taskAnswerGeneralService.getAppraisersForTaskAnswer(taskAnswerId, userId);
    }

    @GetMapping("/task-answer/{taskAnswerId}/appraisers/all")
    @Operation(summary = "All appraiser evaluations [FOR TEACHER+]")
    public List<PeerEvaluationModel> getAllTaskAnswerAppraisers(@PathVariable UUID taskAnswerId,
                                                                   @RequestAttribute("userId") UUID userId) {
        return taskAnswerGeneralService.getAllAppraisersForTaskAnswer(taskAnswerId, userId);
    }
}
