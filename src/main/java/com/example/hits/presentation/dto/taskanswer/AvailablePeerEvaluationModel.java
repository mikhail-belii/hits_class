package com.example.hits.presentation.dto.taskanswer;

import com.example.hits.presentation.dto.file.FileModel;
import com.example.hits.presentation.dto.user.UserModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class AvailablePeerEvaluationModel {

    private UUID taskAnswerId;

    private UserModel student;

    private LocalDateTime submittedAt;

    private Boolean canAppraise;

    private PeerEvaluationUnavailableReason unavailableReason;

    private List<FileModel> files;
}
