package com.example.hits.presentation.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@AllArgsConstructor
public class IdResponseModel {
    private UUID id;
}
