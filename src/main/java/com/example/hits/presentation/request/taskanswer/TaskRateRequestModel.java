package com.example.hits.presentation.request.taskanswer;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TaskRateRequestModel {

    private Float rate;
}
