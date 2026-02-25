package com.example.hits.application.controller;

import com.example.hits.application.model.course.*;
import com.example.hits.application.service.CourseService;
import com.example.hits.domain.entity.user.UserCourseRole;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @Operation(summary = "Create course")
    public void createCourse(
            @RequestAttribute("userId") UUID userId,
            @RequestBody CourseCreateModel courseCreateModel
    ) {
        courseService.createCourse(userId, courseCreateModel);
    }

    @PatchMapping("/{courseId}")
    @Operation(summary = "Edit course")
    public void editCourse(
            @RequestAttribute("userId") UUID userId,
            @PathVariable("courseId") UUID courseId,
            @RequestBody CourseEditModel courseCreateModel
    ) {
        courseService.editCourse(userId, courseId, courseCreateModel);
    }

    @PatchMapping(value = "/{courseId}/archive")
    @Operation(summary = "Archive/Unarchive course")
    public void archiveCourse(@RequestParam boolean isArchived, @PathVariable("courseId") UUID courseId) {

    }

    @GetMapping(value = "/{courseId}/users")
    @Operation(summary = "Get course users")
    public List<UserCourseModel> getCourseUsers(@PathVariable("courseId") UUID courseId) {
        return List.of();
    }

    @GetMapping(value = "/my")
    @Operation(summary = "Get user courses")
    public List<CourseShortModel> getUserCourses(@RequestAttribute("userId") UUID userId, @RequestParam boolean isArchived) {
        return List.of();
    }

    @GetMapping(value = "/{courseId}")
    @Operation(summary = "Get concrete course")
    public List<CourseModel> getConcreteCourse(@RequestAttribute("userId") UUID userId, @PathVariable("courseId") UUID courseId) {
        return List.of();
    }

    @GetMapping(value = "/join")
    @Operation(summary = "Join course by code")
    public void joinCourseByCode(@RequestAttribute("userId") UUID userId, @RequestParam String code) {

    }

    @PostMapping(value = "/{courseId}/users/{userID}/role")
    @Operation(summary = "Change user role in course")
    public void changeUserRoleOnCourse(
            @PathVariable("courseId") UUID courseId,
            @PathVariable("userID") UUID userId,
            @RequestParam UserCourseRole newUserRole
    ) {

    }

    @PostMapping(value = "/{courseId}/users/{userID}/remove")
    @Operation(summary = "Remove user from course")
    public void removeUserFromCourse(
            @PathVariable("courseId") UUID courseId,
            @PathVariable("userID") UUID userId,
            @RequestAttribute("userId") UUID requestingUserId
    ) {

    }

}
