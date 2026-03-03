package com.example.hits.application.util;

import com.example.hits.application.handler.ExceptionWrapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.experimental.UtilityClass;
import org.apache.coyote.BadRequestException;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@UtilityClass
public class ExceptionUtility {
    public ExceptionWrapper courseNotFoundException() {
        var notFoundException = new ExceptionWrapper(new EntityNotFoundException("Course not found"));
        notFoundException.addError("courseId", "Course not found");
        return notFoundException;
    }

    public ExceptionWrapper userCourseNotFoundException() {
        var notFoundException = new ExceptionWrapper(new EntityNotFoundException("UserCourse not found"));
        notFoundException.addError("userCourse", "User is not studying on this course");
        return notFoundException;
    }

    public ExceptionWrapper forbiddenRightsException() {
        var forbiddenException = new ExceptionWrapper(
                new ResponseStatusException(FORBIDDEN, "User has no rights to this action")
        );
        forbiddenException.addError("forbidden", "User has no rights to this action");
        return forbiddenException;
    }

    public ExceptionWrapper userNotFoundException() {
        var notFoundException = new ExceptionWrapper(new EntityNotFoundException("User not found"));
        notFoundException.addError("userId", "User not found");

        return notFoundException;
    }

    public ExceptionWrapper postNotFoundException() {
        var notFoundException = new ExceptionWrapper(new EntityNotFoundException("Post not found"));
        notFoundException.addError("postId", "Post not found");

        return notFoundException;
    }

    public ExceptionWrapper badRequestException(String message) {
        var badRequestException = new ExceptionWrapper(new BadRequestException(message));
        badRequestException.addError("Bad request", message);

        return badRequestException;
    }
}
