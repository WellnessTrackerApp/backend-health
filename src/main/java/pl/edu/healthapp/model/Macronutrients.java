package pl.edu.healthapp.model;

import jakarta.persistence.Embeddable;

@Embeddable
public record Macronutrients(
    int calories,
    double protein,
    double carbs,
    double fat){}
