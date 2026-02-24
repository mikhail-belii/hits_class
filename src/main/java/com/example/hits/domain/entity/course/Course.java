package com.example.hits.domain.entity.course;

import com.example.hits.domain.entity.usercourse.UserCourse;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "course")
@Data
@Accessors(chain = true)
public class Course {

    @Id
    private UUID id;

    @NotNull
    @Length(max = 128)
    private String name;

    @NotNull
    @Length(min = 8, max = 8)
    @Column(unique = true)
    private String joinCode;

    @NotNull
    @Length(max = 512)
    private String description;

    @OneToMany(mappedBy = "course")
    private List<UserCourse> courseUsers;

    @NotNull
    private Boolean isArchived;

    @NotNull
    private LocalDateTime createdAt;
}