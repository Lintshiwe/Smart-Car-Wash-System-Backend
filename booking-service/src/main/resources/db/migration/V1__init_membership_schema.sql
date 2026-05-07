-- Flyway migration: V1__init_membership_schema.sql
-- Creates all membership and booking related tables

-- Create booking_schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS booking_schema;

-- Membership Plans table
CREATE TABLE booking_schema.membership_plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    monthly_price NUMERIC(10, 2) NOT NULL,
    credits_per_month INTEGER NOT NULL,
    free_washes INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    discount_eligible BOOLEAN NOT NULL DEFAULT false,
    discount_percentage NUMERIC(5, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_membership_plans_active ON booking_schema.membership_plans(is_active);
CREATE INDEX idx_membership_plans_discount ON booking_schema.membership_plans(discount_eligible);

-- Memberships table (client subscriptions)
CREATE TABLE booking_schema.memberships (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL UNIQUE,
    plan_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    credits_remaining INTEGER NOT NULL DEFAULT 0,
    washes_used_this_month INTEGER NOT NULL DEFAULT 0,
    auto_renew BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_memberships_plan_id FOREIGN KEY (plan_id) REFERENCES booking_schema.membership_plans(id)
);

CREATE INDEX idx_memberships_client_id ON booking_schema.memberships(client_id);
CREATE INDEX idx_memberships_status ON booking_schema.memberships(status);
CREATE INDEX idx_memberships_expiry ON booking_schema.memberships(expiry_date);

-- Membership Credit Logs table (audit trail)
CREATE TABLE booking_schema.membership_credit_logs (
    id BIGSERIAL PRIMARY KEY,
    membership_id BIGINT NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    credits_changed INTEGER NOT NULL,
    balance_before INTEGER NOT NULL,
    balance_after INTEGER NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_credit_logs_membership_id FOREIGN KEY (membership_id) REFERENCES booking_schema.memberships(id)
);

CREATE INDEX idx_credit_logs_membership_id ON booking_schema.membership_credit_logs(membership_id);
CREATE INDEX idx_credit_logs_type ON booking_schema.membership_credit_logs(transaction_type);
CREATE INDEX idx_credit_logs_date ON booking_schema.membership_credit_logs(created_at);

-- Grant permissions to booking-service user (same user as other services)
GRANT USAGE ON SCHEMA booking_schema TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA booking_schema TO postgres;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA booking_schema TO postgres;
