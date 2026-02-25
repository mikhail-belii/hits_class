package com.example.hits.domain.service.course;

import com.example.hits.application.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.hits.domain.service.course.CourseCodeGenerator.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseCodeGeneratorTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseCodeGenerator generator;

    @Test
    void generateNewCode_whenCanGetUniqueCode_returnsGeneratedCode() {
        when(courseRepository.existsByJoinCode(anyString())).thenReturn(false);

        String result = generator.generateNewCode();

        assertNotNull(result);
        assertEquals(CODE_LENGTH, result.length());
        verify(courseRepository).existsByJoinCode(anyString());
    }

    @Test
    void generateNewCode_whenCanGetUniqueCode_containsOnlyExpectedSymbols() {
        when(courseRepository.existsByJoinCode(anyString())).thenReturn(false);

        String result = generator.generateNewCode();

        boolean containsOnlyExpectedSymbols = true;
        for (char symbol : result.toCharArray()) {
            if (CODE_SYMBOLS.indexOf(symbol) == -1) {
                containsOnlyExpectedSymbols = false;
            }
        }
        assertTrue(containsOnlyExpectedSymbols);
        verify(courseRepository).existsByJoinCode(anyString());
    }

    @Test
    void generateNewCode_whenCanNotGetUniqueCode_throwsRuntimeException() {
        when(courseRepository.existsByJoinCode(anyString())).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> generator.generateNewCode());

        assertTrue(exception.getMessage()
                .contains("Не получилось сгенерировать код после " + MAX_ATTEMPTS + " попыток, попробуйте снова"));
        verify(courseRepository, times(MAX_ATTEMPTS)).existsByJoinCode(anyString());
    }

    @Test
    void generateCode_whenCalledMultipleTimes_returnsDifferentCodes() {
        when(courseRepository.existsByJoinCode(anyString())).thenReturn(false);

        String code1 = generator.generateNewCode();
        String code2 = generator.generateNewCode();
        String code3 = generator.generateNewCode();

        assertNotEquals(code1, code2);
        assertNotEquals(code1, code3);
        assertNotEquals(code2, code3);

        verify(courseRepository, times(3)).existsByJoinCode(anyString());
    }
}
