package com.example.hits.presentation.dto.taskanswer;

public enum PeerEvaluationUnavailableReason {
    TASK_DEADLINE_HAS_NOT_PASSED,
    APPRAISER_DEADLINE_HAS_PASSED,
    ANSWER_IS_NOT_SUBMITTED,
    OWN_ANSWER,
    ALREADY_SELECTED,
    APPRAISING_LIMIT_REACHED,
    RECIPROCAL_APPRAISING
}
