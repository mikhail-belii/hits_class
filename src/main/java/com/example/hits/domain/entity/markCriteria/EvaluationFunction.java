package com.example.hits.domain.entity.markCriteria;

public enum EvaluationFunction {
    SUM {
        @Override
        public Float performEvaluation(Float a, Float b) {
            return a + b;
        }
    },
    MULTIPLY {
        @Override
        public Float performEvaluation(Float a, Float b) {
            return a * b;
        }
    };

    public abstract Float performEvaluation(Float a, Float b);

}
