-- Flyway migration: V3__add_notifications_table.sql
-- Creates notifications table for in-app notifications

CREATE TABLE booking_schema.notifications (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    details TEXT,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    read_at TIMESTAMP
);

CREATE INDEX idx_notifications_client_id ON booking_schema.notifications(client_id);
CREATE INDEX idx_notifications_type ON booking_schema.notifications(notification_type);
CREATE INDEX idx_notifications_read ON booking_schema.notifications(is_read);
CREATE INDEX idx_notifications_created ON booking_schema.notifications(created_at);
