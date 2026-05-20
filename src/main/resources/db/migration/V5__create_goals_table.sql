CREATE TABLE goals (
                       id SERIAL PRIMARY KEY,
                       healthGoalType VARCHAR(50) NOT NULL,
                       target DOUBLE PRECISION NOT NULL,
                       createdAt TIMESTAMP WITH TIME ZONE NOT NULL,
                       user_id UUID NOT NULL,
                       CONSTRAINT health_goal_user
                           FOREIGN KEY (user_id)
                               REFERENCES health_users(id)
                               ON DELETE CASCADE
);