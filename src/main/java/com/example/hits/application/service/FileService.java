package com.example.hits.application.service;

import com.example.hits.application.handler.ExceptionWrapper;
import com.example.hits.application.model.file.FileModel;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileService {
    FileModel upload(UUID userId, MultipartFile file) throws ExceptionWrapper;
    ResponseEntity<Resource> getById(UUID fileId);
}
