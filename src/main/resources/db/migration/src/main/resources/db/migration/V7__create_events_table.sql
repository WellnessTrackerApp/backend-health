CREATE TABLE events (
                       id SERIAL PRIMARY KEY,
                       entityId BIGINT NOT NULL,
                       eventType VARCHAR(55) NOT NULL,
                       createdAt TIMESTAMP WITH TIME ZONE NOT NULL,
                       user_id BIGINT NOT NULL,
                       payload TEXT NOT NULL,
                       CONSTRAINT event_user
                           FOREIGN KEY (user_id)
                               REFERENCES users(id)
                               ON DELETE CASCADE
);