package com.example.hits.application.handler;

import com.example.hits.application.model.common.Pair;
import com.example.hits.application.model.common.ResponseModel;
import jakarta.persistence.EntityNotFoundException;
import jakarta.security.auth.message.AuthException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.util.Dictionary;
import java.util.Hashtable;

@RestControllerAdvice
public class CustomExceptionHandler {
    private Pair<Integer, HttpStatus> getStatusCodeAndHttpStatusByExceptionClass(Class<? extends Exception> exceptionClass) {
        if(exceptionClass.equals(EntityNotFoundException.class)) {
            return new Pair<>(404, HttpStatus.NOT_FOUND);
        }
        else if(exceptionClass.equals(ConstraintViolationException.class) ||
                exceptionClass.equals(MethodArgumentNotValidException.class) ||
                exceptionClass.equals(NoResourceFoundException.class) ||
                exceptionClass.equals(ValidationException.class) ||
                exceptionClass.equals(HttpMessageNotReadableException.class) ||
                exceptionClass.equals(BadRequestException.class)) {
            return new Pair<>(400, HttpStatus.BAD_REQUEST);
        }
        else if(exceptionClass.equals(AuthException.class)) {
            return new Pair<>(401, HttpStatus.UNAUTHORIZED);
        }
        else if(exceptionClass.equals(AccessDeniedException.class)) {
            return new Pair<>(403, HttpStatus.FORBIDDEN);
        }

        return new Pair<>(500, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ExceptionWrapper.class)
    public ResponseEntity<ResponseModel> handleAllWrappedExceptions(
            ExceptionWrapper e
    ) {
        Pair<Integer, HttpStatus> statusCodeAndHttpStatus = getStatusCodeAndHttpStatusByExceptionClass(e.getExceptionClass());
        ResponseModel response = new ResponseModel(statusCodeAndHttpStatus.first(), e.getErrors());
        return new ResponseEntity<>(response, statusCodeAndHttpStatus.second());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseModel> handleConstraintValidationException(
            ConstraintViolationException e
    ) {
        Dictionary<String, String> errors = new Hashtable<>();
        e.getConstraintViolations()
                .forEach(
                        violation -> errors.put(violation.getPropertyPath().toString(), violation.getMessage())
                );
        Pair<Integer, HttpStatus> statusCodeAndHttpStatus = getStatusCodeAndHttpStatusByExceptionClass(e.getClass());
        ResponseModel response = new ResponseModel(statusCodeAndHttpStatus.first(), errors);
        return new ResponseEntity<>(response, statusCodeAndHttpStatus.second());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseModel> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        Dictionary<String, String> errors = new Hashtable<>();
        e.getBindingResult().getFieldErrors()
                .forEach(
                        violation -> errors.put(violation.getField(), violation.getDefaultMessage())
                );
        Pair<Integer, HttpStatus> statusCodeAndHttpStatus = getStatusCodeAndHttpStatusByExceptionClass(e.getClass());
        ResponseModel response = new ResponseModel(statusCodeAndHttpStatus.first(), errors);
        return new ResponseEntity<>(response, statusCodeAndHttpStatus.second());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ResponseModel> handleValidationException(
            ValidationException e
    ) {
        Pair<Integer, HttpStatus> statusCodeAndHttpStatus = getStatusCodeAndHttpStatusByExceptionClass(e.getClass());
        Dictionary<String, String> errors = new Hashtable<>();
        errors.put("Validation", e.getMessage());
        ResponseModel response = new ResponseModel(statusCodeAndHttpStatus.first(), errors);
        return new ResponseEntity<>(response, statusCodeAndHttpStatus.second());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseModel> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e
    ) {
        Pair<Integer, HttpStatus> statusCodeAndHttpStatus = getStatusCodeAndHttpStatusByExceptionClass(e.getClass());
        Dictionary<String, String> errors = new Hashtable<>();
        errors.put("JsonBody", e.getMessage());
        ResponseModel response = new ResponseModel(statusCodeAndHttpStatus.first(), errors);
        return new ResponseEntity<>(response, statusCodeAndHttpStatus.second());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ResponseModel> handleNoResourceFoundException(
            NoResourceFoundException e
    ) {
        Pair<Integer, HttpStatus> statusCodeAndHttpStatus = getStatusCodeAndHttpStatusByExceptionClass(e.getClass());
        Dictionary<String, String> errors = new Hashtable<>();
        errors.put("url", e.getMessage());
        ResponseModel response = new ResponseModel(statusCodeAndHttpStatus.first(), errors);
        return new ResponseEntity<>(response, statusCodeAndHttpStatus.second());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseModel> handleNotKnownException(
            Exception e
    ) {
        Pair<Integer, HttpStatus> statusCodeAndHttpStatus = getStatusCodeAndHttpStatusByExceptionClass(e.getClass());
        var a = e.getMessage();
        Dictionary<String, String> errors = new Hashtable<>();
        errors.put("unknownError", "Unknown error, please tell us about it");
        System.out.println(e.getMessage());
        ResponseModel response = new ResponseModel(statusCodeAndHttpStatus.first(), errors);
        return new ResponseEntity<>(response, statusCodeAndHttpStatus.second());
    }
}
