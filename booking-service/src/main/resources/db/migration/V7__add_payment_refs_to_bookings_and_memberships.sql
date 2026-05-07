ALTER TABLE booking_schema.bookings
    ADD COLUMN IF NOT EXISTS payment_reference VARCHAR(64),
    ADD COLUMN IF NOT EXISTS payment_status VARCHAR(20);

ALTER TABLE booking_schema.memberships
    ADD COLUMN IF NOT EXISTS latest_payment_reference VARCHAR(64),
    ADD COLUMN IF NOT EXISTS latest_payment_status VARCHAR(20);
