package com.example.hits.application.util;

import com.example.hits.application.handler.ExceptionWrapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.experimental.UtilityClass;
import org.apache.coyote.BadRequestException;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@UtilityClass
public class ExceptionUtility {
    public ExceptionWrapper courseNotFoundException() {
        var notFoundException = new ExceptionWrapper(new EntityNotFoundException("Course not found"));
        notFoundException.addError("courseId", "Cannot find course with requested id");
        return notFoundException;
    }

    public ExceptionWrapper courseNotFoundByCodeException() {
        var notFoundException = new ExceptionWrapper(new EntityNotFoundException("Course with such join code not found"));
        notFoundException.addError("joinCode", "Cannot find course with requested joinCode");
        return notFoundException;
    }

    public ExceptionWrapper userCourseNotFoundException() {
        var notFoundException = new ExceptionWrapper(new EntityNotFoundException("UserCourse not found"));
        notFoundException.addError("userCourse", "User is not studying on this course");
        return notFoundException;
    }

    public ExceptionWrapper userAlreadyParticipantInCourseException() {
        var notFoundException = new ExceptionWrapper(new BadRequestException("User is already a participant in the course"));
        notFoundException.addError("joinCode", "User is already a participant in the course with this joinCode");
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
