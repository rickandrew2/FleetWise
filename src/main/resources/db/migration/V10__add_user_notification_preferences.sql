ALTER TABLE users
    ADD COLUMN notification_email VARCHAR(100);

ALTER TABLE users
    ADD COLUMN email_notifications_enabled BOOLEAN NOT NULL DEFAULT FALSE;
