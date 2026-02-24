package com.example.hits.domain.service.course;

import com.example.hits.application.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseCodeGenerator {

    private final CourseRepository courseRepository;

    public String generateNewCode() {
        return "";
    }

}
