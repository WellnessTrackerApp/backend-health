CREATE TABLE sleep_entries (
                       id SERIAL PRIMARY KEY,
                       startSleep TIMESTAMP WITH TIME ZONE NOT NULL,
                       sleepEnd TIMESTAMP WITH TIME ZONE NOT NULL,
                       quality VARCHAR(50) NOT NULL,
                       user_id UUID NOT NULL,
                       CONSTRAINT sleep_user
                           FOREIGN KEY (user_id)
                               REFERENCES health_users(id)
                               ON DELETE CASCADE
);