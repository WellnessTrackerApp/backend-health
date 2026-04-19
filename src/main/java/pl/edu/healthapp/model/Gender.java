package pl.edu.healthapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Gender {
    MALE(0.415, 5),
    FEMALE(0.413, -161),
    UNSPECIFIED(0.414, 0);


    @Getter
    private final double stepLengthMultiplier;

    @Getter
    private final double calorieNormFactor;
}
