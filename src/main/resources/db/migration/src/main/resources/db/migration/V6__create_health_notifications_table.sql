CREATE TABLE health_notifications (
                       id SERIAL PRIMARY KEY,
                       message VARCHAR(255) NOT NULL,
                       notificationType VARCHAR(55) NOT NULL,
                       updatedAt TIMESTAMP WITH TIME ZONE NOT NULL,
                       user_id BIGINT NOT NULL,
                       CONSTRAINT notification_user
                           FOREIGN KEY (user_id)
                               REFERENCES users(id)
                               ON DELETE CASCADE
);