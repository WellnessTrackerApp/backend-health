package pl.edu.healthapp.model;

import lombok.Getter;

public enum SleepQuality {
    VERY_POOR(1),
    POOR(2),
    FAIR(3),
    GOOD(4),
    EXCELLENT(5),
    UNSPECIFIED(-1);

    @Getter
    private final int rating;

    SleepQuality(int rating) {
        this.rating = rating;
    }

    public static SleepQuality fromRating(int rating){
        for (SleepQuality q : SleepQuality.values())
            if (q.getRating() == rating)
                return q;

        return UNSPECIFIED;
    }

    public static SleepQuality fromAverageRating(double average){
        return fromRating((int) Math.round(average));
    }
}
