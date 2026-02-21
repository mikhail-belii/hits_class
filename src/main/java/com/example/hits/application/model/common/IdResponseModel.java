package com.example.hits.application.model.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@AllArgsConstructor
public class IdResponseModel {
    private UUID id;
}
