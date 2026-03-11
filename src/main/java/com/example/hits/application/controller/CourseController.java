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
    public CourseModel createCourse(
            @RequestAttribute("userId") UUID requestingUserId,
            @RequestBody CourseCreateModel courseCreateModel
    ) {
        return courseService.createCourse(requestingUserId, courseCreateModel);
    }

    @PatchMapping("/{courseId}")
    @Operation(summary = "Edit course")
    public void editCourse(
            @RequestAttribute("userId") UUID requestingUserId,
            @PathVariable("courseId") UUID courseId,
            @RequestBody CourseEditModel courseCreateModel
    ) {
        courseService.editCourse(requestingUserId, courseId, courseCreateModel);
    }

    @PatchMapping(value = "/{courseId}/archive")
    @Operation(summary = "Archive/Unarchive course")
    public void archiveCourse(
            @RequestAttribute("userId") UUID requestingUserId,
            @RequestParam boolean isArchived,
            @PathVariable("courseId") UUID courseId
    ) {
        courseService.archiveCourse(requestingUserId, isArchived, courseId);
    }

    @GetMapping(value = "/{courseId}/users")
    @Operation(summary = "Get course users")
    public List<UserCourseModel> getCourseUsers(
            @RequestAttribute("userId") UUID requestingUserId,
            @PathVariable("courseId") UUID courseId
    ) {
        return courseService.getCourseUsers(requestingUserId, courseId);
    }

    @GetMapping(value = "/my")
    @Operation(summary = "Get user courses")
    public List<CourseShortModel> getUserCourses(
            @RequestAttribute("userId") UUID requestingUserId,
            @RequestParam boolean isArchived
    ) {
        return courseService.getUserCourses(requestingUserId, isArchived);
    }

    @GetMapping(value = "/{courseId}")
    @Operation(summary = "Get concrete course")
    public CourseModel getConcreteCourse(
            @RequestAttribute("userId") UUID requestingUserId,
            @PathVariable("courseId") UUID courseId
    ) {
        return courseService.getConcreteCourse(requestingUserId, courseId);
    }

    @GetMapping(value = "/join")
    @Operation(summary = "Join course by code")
    public void joinCourseByCode(
            @RequestAttribute("userId") UUID requestingUserId,
            @RequestParam String code
    ) {
        courseService.joinCourseByCode(requestingUserId, code);
    }

    @PostMapping(value = "/{courseId}/users/{userID}/role")
    @Operation(summary = "Change user role in course")
    public void changeUserRoleOnCourse(
            @RequestAttribute("userId") UUID requestingUserId,
            @PathVariable("courseId") UUID courseId,
            @PathVariable("userID") UUID userId,
            @RequestParam UserCourseRole newUserRole
    ) {
        courseService.changeUserRoleOnCourse(requestingUserId, courseId, userId, newUserRole);
    }

    @PostMapping(value = "/{courseId}/users/{userID}/remove")
    @Operation(summary = "Remove user from course")
    public void removeUserFromCourse(
            @PathVariable("courseId") UUID courseId,
            @PathVariable("userID") UUID userId,
            @RequestAttribute("userId") UUID requestingUserId
    ) {
        courseService.removeUserFromCourse(requestingUserId, courseId, userId);
    }

    @PostMapping(value = "/{courseId}/leave")
    @Operation(summary = "Remove user from course")
    public void leaveFromCourse(
            @PathVariable("courseId") UUID courseId,
            @RequestAttribute("userId") UUID requestingUserId
    ) {
        courseService.leaveFromCourse(requestingUserId, courseId);
    }

}
