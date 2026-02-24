package com.example.hits.application.controller;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.model.common.ResponseModel;
import com.example.hits.application.model.file.FileModel;
import com.example.hits.application.service.FileService;
import com.example.hits.application.util.ExceptionUtility;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.Set;

@RestController
@RequestMapping("api/v1/file")
@AllArgsConstructor
public class FileController {
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("txt", "docx", "png", "pdf");

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Upload file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "File was uploaded",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FileModel.class)
                    )}),
            @ApiResponse(responseCode = "400",
                    description = "Bad request",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseModel.class)
                    )})
    })
    public FileModel upload(@RequestParam("file") MultipartFile file,
                            @RequestAttribute("userId") String userId) throws ExceptionWrapper {
        validateSize(file);
        validateExtension(file);

        return fileService.upload(UUID.fromString(userId), file);
    }

    @GetMapping("/{fileId}")
    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Get file by id")
    @ApiResponse(responseCode = "200",
            description = "File was received",
            content = {@Content(mediaType = "application/octet-stream")})
    public ResponseEntity<Resource> getById(@PathVariable UUID fileId) {
        return fileService.getById(fileId);
    }

    private void validateSize(MultipartFile file) throws ExceptionWrapper {
        if (file == null || file.isEmpty()) {
            throw ExceptionUtility.badRequestException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw ExceptionUtility.badRequestException("File size must be less than or equal to 10 MB");
        }
    }

    private void validateExtension(MultipartFile file) throws ExceptionWrapper {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank() || !fileName.contains(".")) {
            throw ExceptionUtility.badRequestException("File extension is required");
        }

        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw ExceptionUtility.badRequestException("Allowed extensions: txt, docx, png, pdf");
        }
    }
}
