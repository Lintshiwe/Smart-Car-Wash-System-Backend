CREATE TABLE IF NOT EXISTS booking_schema.payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    reference VARCHAR(64) NOT NULL UNIQUE,
    client_id BIGINT NOT NULL,
    booking_id BIGINT,
    membership_id BIGINT,
    purpose VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    gateway VARCHAR(20) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    gateway_transaction_id VARCHAR(120),
    failure_reason VARCHAR(255),
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_payment_tx_client_id ON booking_schema.payment_transactions(client_id);
CREATE INDEX IF NOT EXISTS idx_payment_tx_booking_id ON booking_schema.payment_transactions(booking_id);
CREATE INDEX IF NOT EXISTS idx_payment_tx_membership_id ON booking_schema.payment_transactions(membership_id);
CREATE INDEX IF NOT EXISTS idx_payment_tx_reference ON booking_schema.payment_transactions(reference);
CREATE INDEX IF NOT EXISTS idx_payment_tx_status ON booking_schema.payment_transactions(status);
CREATE INDEX IF NOT EXISTS idx_payment_tx_created_at ON booking_schema.payment_transactions(created_at);
