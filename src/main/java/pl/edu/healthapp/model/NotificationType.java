package pl.edu.healthapp.model;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum NotificationType {
    SLEEP,
    LOW_KCAL,
    LOW_PROTEIN,
    LOW_CARBS,
    LOW_FAT,
    ACTIVITY;
}
