CREATE TABLE activities (
                       id SERIAL PRIMARY KEY,
                       startedAt TIMESTAMP WITH TIME ZONE NOT NULL,
                       activityType VARCHAR(50) NOT NULL,
                       durationInMinutes INT NOT NULL,
                       caloriesBurned DOUBLE PRECISION NOT NULL,
                       user_id BIGINT NOT NULL,
                       CONSTRAINT activity_user
                           FOREIGN KEY (user_id)
                               REFERENCES users(id)
                               ON DELETE CASCADE
);