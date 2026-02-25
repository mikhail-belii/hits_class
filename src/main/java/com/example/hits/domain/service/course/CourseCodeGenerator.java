package com.example.hits.domain.service.course;

import com.example.hits.application.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseCodeGenerator {

    private final CourseRepository courseRepository;

    public static final String CODE_SYMBOLS = "йцукенгшщзхъфывапролджэячсмитьбюЙЦУКЕНГШЩЗХЪФЫВАПРОЛДЖЭЯЧСМИТЬБЮ1234567890";
    public static final int CODE_LENGTH = 8;
    public static final int MAX_ATTEMPTS = 20;

    public String generateNewCode() {
        return "";
    }

}
