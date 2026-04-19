CREATE TABLE goals (
                       id SERIAL PRIMARY KEY,
                       healthGoalType VARCHAR(50) NOT NULL,
                       target DOUBLE PRECISION NOT NULL,
                       createdAt TIMESTAMP WITH TIME ZONE NOT NULL,
                       user_id BIGINT NOT NULL,
                       CONSTRAINT health_goal_user
                           FOREIGN KEY (user_id)
                               REFERENCES users(id)
                               ON DELETE CASCADE
);