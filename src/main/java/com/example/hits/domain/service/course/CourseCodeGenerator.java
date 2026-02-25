package com.example.hits.domain.service.course;

import com.example.hits.application.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class CourseCodeGenerator {

    private final CourseRepository courseRepository;

    public static final String CODE_SYMBOLS = "йцукенгшщзхъфывапролджэячсмитьбюЙЦУКЕНГШЩЗХЪФЫВАПРОЛДЖЭЯЧСМИТЬБЮ1234567890";
    public static final int CODE_LENGTH = 8;
    public static final int MAX_ATTEMPTS = 20;

    public String generateNewCode() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = generateCode();
            if (!courseRepository.existsByJoinCode(code)) {
                return code;
            }
        }
        throw new RuntimeException("Не получилось сгенерировать код после " + MAX_ATTEMPTS + " попыток, попробуйте снова");
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODE_SYMBOLS.charAt(random.nextInt(CODE_SYMBOLS.length())));
        }
        return code.toString();
    }

}
