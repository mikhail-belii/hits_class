package com.example.hits.domain.service.taskanswer;

import com.example.hits.application.repository.TaskAnswerRepository;
import com.example.hits.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskAnswerUploadService {

    private final TaskAnswerRepository taskAnswerRepository;
    private final UserRepository userRepository;


}
