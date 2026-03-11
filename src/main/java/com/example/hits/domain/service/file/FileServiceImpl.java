package com.example.hits.domain.service.file;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.model.file.FileModel;
import com.example.hits.application.repository.FileRepository;
import com.example.hits.application.repository.UserRepository;
import com.example.hits.application.service.FileService;
import com.example.hits.domain.entity.file.File;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ValidationException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    @Value("${file-storage-root:files/uploads}")
    private String storageRoot;

    @Override
    @Transactional
    public FileModel upload(UUID userId, MultipartFile file) {
        var userOpt =  userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            var entityNotFoundEx = new ExceptionWrapper(new EntityNotFoundException());
            entityNotFoundEx.addError("User", "User not found");
            throw entityNotFoundEx;
        }

        var user = userOpt.get();

        if (file == null || file.isEmpty()) {
            var validationEx =  new ExceptionWrapper(new ValidationException());
            validationEx.addError("File", "File is empty");
            throw validationEx;
        }

        var fileId = UUID.randomUUID();
        var extension = getFileExtension(file.getOriginalFilename());
        var fileName = extension.isEmpty() ? fileId.toString() : fileId + "." + extension;

        var relativeDirectory = Paths.get(storageRoot);
        var absoluteDirectory = relativeDirectory.isAbsolute()
                ? relativeDirectory
                : Paths.get(System.getProperty("user.dir")).resolve(relativeDirectory);
        var targetPath = absoluteDirectory.resolve(fileName);

        try {
            Files.createDirectories(absoluteDirectory);
            file.transferTo(targetPath);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save file", exception);
        }

        registerDeleteOnRollback(targetPath);

        var savedFile = new File()
                .setId(fileId)
                .setUploader(user)
                .setPath(relativeDirectory.resolve(fileName).toString().replace('\\', '/'))
                .setOriginalName(resolveOriginalName(file.getOriginalFilename()))
                .setCreatedAt(LocalDateTime.now());

        fileRepository.save(savedFile);

        return new FileModel(savedFile.getId(), savedFile.getOriginalName());
    }

    @Override
    public ResponseEntity<Resource> getById(UUID fileId) {
        var fileOpt = fileRepository.findById(fileId);
        if (fileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var file = fileOpt.get();

        var filePath = resolveAbsolutePath(file.getPath());
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return ResponseEntity.notFound().build();
        }

        var resource = toResource(filePath);
        var mediaType = resolveMediaType(filePath);
        var disposition = ContentDisposition.attachment()
                .filename(file.getOriginalName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }

    private String getFileExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "";
        }

        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex < 0 || lastDotIndex == originalFilename.length() - 1) {
            return "";
        }

        return originalFilename.substring(lastDotIndex + 1);
    }

    private String resolveOriginalName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "file";
        }

        return originalFilename;
    }

    private Path resolveAbsolutePath(String storedPath) {
        var path = Paths.get(storedPath);
        return path.isAbsolute() ? path : Paths.get(System.getProperty("user.dir")).resolve(path).normalize();
    }

    private Resource toResource(Path filePath) {
        try {
            return new UrlResource(filePath.toUri());
        } catch (MalformedURLException exception) {
            throw new IllegalStateException("Failed to build file resource", exception);
        }
    }

    private MediaType resolveMediaType(Path filePath) {
        try {
            var contentType = Files.probeContentType(filePath);
            if (contentType == null || contentType.isBlank()) {
                return MediaType.APPLICATION_OCTET_STREAM;
            }
            return MediaType.parseMediaType(contentType);
        } catch (IOException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private void registerDeleteOnRollback(Path targetPath) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    try {
                        Files.deleteIfExists(targetPath);
                    } catch (IOException ignored) {
                    }
                }
            }
        });
    }
}
