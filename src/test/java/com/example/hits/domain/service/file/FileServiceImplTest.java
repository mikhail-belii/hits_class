package com.example.hits.domain.service.file;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.repository.FileRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.domain.entity.file.File;
import com.example.hits.domain.entity.user.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private FileServiceImpl fileService;

    @TempDir
    Path tempDir;

    @Test
    void upload_validFile_savesFileAndEntity() {
        ReflectionTestUtils.setField(fileService, "storageRoot", tempDir.toString());
        UUID userId = UUID.randomUUID();
        User user = new User().setId(userId);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "answer.txt",
                "text/plain",
                "content".getBytes()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(fileRepository.save(any(File.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = fileService.upload(userId, multipartFile);

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getId());

        ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
        verify(fileRepository).save(fileCaptor.capture());
        File savedFile = fileCaptor.getValue();

        Assertions.assertEquals(userId, savedFile.getUploader().getId());
        Assertions.assertEquals("answer.txt", savedFile.getOriginalName());
        Assertions.assertTrue(savedFile.getPath().endsWith(".txt"));
        Assertions.assertNotNull(savedFile.getCreatedAt());

        Path createdFile = tempDir.resolve(savedFile.getPath().substring(savedFile.getPath().lastIndexOf('/') + 1));
        Assertions.assertTrue(Files.exists(createdFile));
    }

    @Test
    void upload_userNotFound_throwsEntityNotFoundException() {
        UUID userId = UUID.randomUUID();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "file.txt",
                "text/plain",
                "content".getBytes()
        );
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> fileService.upload(userId, multipartFile)
        );

        Assertions.assertEquals(EntityNotFoundException.class, exception.getExceptionClass());
        Assertions.assertEquals("User not found", exception.getErrors().get("User"));
    }

    @Test
    void upload_emptyFile_throwsValidationException() {
        UUID userId = UUID.randomUUID();
        User user = new User().setId(userId);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ExceptionWrapper exception = Assertions.assertThrows(
                ExceptionWrapper.class,
                () -> fileService.upload(userId, multipartFile)
        );

        Assertions.assertEquals(ValidationException.class, exception.getExceptionClass());
        Assertions.assertEquals("File is empty", exception.getErrors().get("File"));
    }

    @Test
    void getById_fileNotFoundInDatabase_returns404() {
        UUID fileId = UUID.randomUUID();
        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = fileService.getById(fileId);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertNull(response.getBody());
    }

    @Test
    void getById_existingFile_returnsResource() throws Exception {
        Path storedFile = tempDir.resolve("stored.pdf");
        Files.writeString(storedFile, "file-content");
        UUID fileId = UUID.randomUUID();
        File file = new File()
                .setId(fileId)
                .setPath(storedFile.toString())
                .setOriginalName("report.pdf");

        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        var response = fileService.getById(fileId);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertTrue(response.getHeaders().containsKey(HttpHeaders.CONTENT_DISPOSITION));
    }
}
