package com.example.hits.domain.entity.file;

import com.example.hits.domain.entity.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "file")
@Data
@Accessors(chain = true)
public class File {
    @Id
    private UUID id;
    @NotNull
    @Length(max = 256)
    private String path;
    @NotNull
    @Length(max = 256)
    private String originalName;
    @ManyToOne
    @JoinColumn(name = "uploader_id")
    private User uploader;
    @NotNull
    private LocalDateTime createdAt;
}