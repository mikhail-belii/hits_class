package com.example.hits.domain.utility;

public class MathUtility {

    public static Float getValueByDiapasons(Float minValue, Float value, Float maxValue) {
        return Math.min(maxValue, Math.max(minValue, value));
    }

}
