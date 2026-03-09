package com.example.hits.application.controller;

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

    @PostMapping("/submit/{taskId}")
    public void submitTask(@PathVariable UUID taskId, @RequestAttribute("userId") UUID userId) {

    }
}
