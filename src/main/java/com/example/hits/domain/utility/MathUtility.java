package com.example.hits.domain.utility;

import java.util.Objects;

public class MathUtility {

    public static Float getValueByDiapasons(Float minValue, Float value, Float maxValue) {
        return Math.min(maxValue, Math.max(minValue, value));
    }

    public static Float interpolateValueByDiapasons(
            Float firstMinValue,
            Float value,
            Float firstMaxValue,
            Float secondMinValue,
            Float secondMaxValue) {

        if (Objects.equals(firstMaxValue, firstMinValue)) {
            return secondMinValue;
        }

        float interpolatedPosition = (value - firstMinValue) / (firstMaxValue - firstMinValue);
        return interpolatedPosition * (secondMaxValue - secondMinValue) + secondMinValue;
    }

}
