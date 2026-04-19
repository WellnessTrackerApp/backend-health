package pl.edu.healthapp.model;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum ActivityType {
    YOGA(3.4),
    WALKING(3.5),
    GOLF(4.4),
    BADMINTON(4.5),
    TENNIS(5.0),
    PILATES(6.0),
    CYCLING(6.0),
    WEIGHT_LIFTING(6.0),
    DANCING(6.5),
    ICE_SKATING(7.0),
    BASKETBALL(8.0),
    VOLLEYBALL(8.0),
    SQUASH(8.5),
    FOOTBALL(8.5),
    SKIING(8.5),
    SWIMMING(9.5),
    SKATING(9.5),
    WATER_POLO(10.0),
    RUNNING(13.0);

    protected final double MET;

    public double caloriesBurned(int duration, double weight){
        return duration * MET * 3.5 * weight / 200;
    }
}
