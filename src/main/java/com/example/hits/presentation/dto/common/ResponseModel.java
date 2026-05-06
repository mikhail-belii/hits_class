package com.example.hits.presentation.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Dictionary;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResponseModel {
    private Integer statusCode;
    private Dictionary<String, String> errors;
}