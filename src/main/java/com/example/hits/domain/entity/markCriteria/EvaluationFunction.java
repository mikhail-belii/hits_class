package com.example.hits.domain.entity.markCriteria;

public enum EvaluationFunction {
    SUM {
        @Override
        Float performEvaluation(Float a, Float b) {
            return a + b;
        }
    },
    MULTIPLY {
        @Override
        Float performEvaluation(Float a, Float b) {
            return a * b;
        }
    };

    abstract Float performEvaluation(Float a, Float b);

}
