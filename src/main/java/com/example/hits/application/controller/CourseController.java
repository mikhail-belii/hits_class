package com.example.hits.application.controller;

import com.example.hits.application.model.course.*;
import com.example.hits.application.service.CourseService;
import com.example.hits.domain.entity.user.UserCourseRole;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping("/{courseId}")
    public void createCourse(@RequestBody CourseCreateModel courseCreateModel) {

    }

    @PatchMapping("/{courseId}")
    public void editCourse(@PathVariable("courseId") UUID courseId, @RequestBody CourseEditModel courseCreateModel) {

    }

    @PatchMapping(value = "/{courseId}/archive")
    public void archiveCourse(@RequestParam boolean isArchived, @PathVariable("courseId") UUID courseId) {

    }

    @GetMapping(value = "/{courseId}/users")
    public List<UserCourseModel> getCourseUsers(@PathVariable("courseId") UUID courseId) {
        return List.of();
    }

    @GetMapping(value = "/my")
    public List<CourseShortModel> getUserCourses(@RequestAttribute("userId") UUID userId, @RequestParam boolean isArchived) {
        return List.of();
    }

    @GetMapping(value = "/{courseId}")
    public List<CourseModel> getConcreteCourse(@RequestAttribute("userId") UUID userId, @PathVariable("courseId") UUID courseId) {
        return List.of();
    }

    @GetMapping(value = "/join")
    public void joinCourseByCode(@RequestAttribute("userId") UUID userId, @RequestParam String code) {

    }

    @PostMapping(value = "/{courseId}/users/{userID}/role")
    public void changeUserRoleOnCourse(
            @PathVariable("courseId") UUID courseId,
            @PathVariable("userID") UUID userId,
            @RequestParam UserCourseRole newUserRole
    ) {

    }

    @PostMapping(value = "/{courseId}/users/{userID}/remove")
    public void removeUserFromCourse(
            @PathVariable("courseId") UUID courseId,
            @PathVariable("userID") UUID userId,
            @RequestAttribute("userId") UUID requestingUserId
    ) {

    }

}
