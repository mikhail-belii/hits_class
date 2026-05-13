package com.example.hits.presentation.controller;

import com.example.hits.application.service.markcriteria.MarkCriteriaService;
import com.example.hits.presentation.dto.common.IdResponseModel;
import com.example.hits.presentation.dto.markcriteria.MarkCriteriaModel;
import com.example.hits.presentation.request.markcriteria.MarkCriteriaWriteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/posts/{postId}/mark-criteria")
@RequiredArgsConstructor
public class MarkCriteriaController {

    private final MarkCriteriaService markCriteriaService;

    @GetMapping
    public List<MarkCriteriaModel> getMarkCriteria(@PathVariable UUID courseId,
                                                   @PathVariable UUID postId,
                                                   @RequestAttribute("userId") UUID userId) {
        return markCriteriaService.getMarkCriteria(courseId, postId, userId);
    }

    @PostMapping
    public IdResponseModel createMarkCriteria(@PathVariable UUID courseId,
                                              @PathVariable UUID postId,
                                              @RequestBody MarkCriteriaWriteRequest request,
                                              @RequestAttribute("userId") UUID userId) {
        return markCriteriaService.createMarkCriteria(courseId, postId, userId, request);
    }

    @PutMapping("/{markCriteriaId}")
    public void updateMarkCriteria(@PathVariable UUID courseId,
                                   @PathVariable UUID postId,
                                   @PathVariable UUID markCriteriaId,
                                   @RequestBody MarkCriteriaWriteRequest request,
                                   @RequestAttribute("userId") UUID userId) {
        markCriteriaService.updateMarkCriteria(courseId, postId, markCriteriaId, userId, request);
    }

    @DeleteMapping("/{markCriteriaId}")
    public void deleteMarkCriteria(@PathVariable UUID courseId,
                                   @PathVariable UUID postId,
                                   @PathVariable UUID markCriteriaId,
                                   @RequestAttribute("userId") UUID userId) {
        markCriteriaService.deleteMarkCriteria(courseId, postId, markCriteriaId, userId);
    }
}
