CREATE TABLE diet_entries (
                       id SERIAL PRIMARY KEY,
                       mealName VARCHAR(255) NOT NULL,
                       calories INT NOT NULL,
                       protein DOUBLE PRECISION NOT NULL,
                       carbs DOUBLE PRECISION NOT NULL,
                       fat DOUBLE PRECISION NOT NULL,
                       eaten_at TIMESTAMP WITH TIME ZONE NOT NULL,
                       user_id BIGINT NOT NULL,
                       CONSTRAINT diet_user
                           FOREIGN KEY (user_id)
                               REFERENCES users(id)
                               ON DELETE CASCADE
);