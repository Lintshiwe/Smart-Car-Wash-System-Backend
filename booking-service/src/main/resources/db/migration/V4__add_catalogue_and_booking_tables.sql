-- Task 4 + 5 schema additions: catalogue + booking core

CREATE TABLE IF NOT EXISTS booking_schema.catalogue_services (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    service_type VARCHAR(20) NOT NULL,
    base_price NUMERIC(10, 2) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_catalogue_services_active ON booking_schema.catalogue_services(is_active);
CREATE INDEX IF NOT EXISTS idx_catalogue_services_type ON booking_schema.catalogue_services(service_type);

CREATE TABLE IF NOT EXISTS booking_schema.catalogue_addons (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    price NUMERIC(10, 2) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_catalogue_addons_active ON booking_schema.catalogue_addons(is_active);

CREATE TABLE IF NOT EXISTS booking_schema.bookings (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    service_code VARCHAR(50) NOT NULL,
    service_type VARCHAR(20) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    vehicle_type VARCHAR(50) NOT NULL,
    location VARCHAR(255) NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    add_ons TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_bookings_client_id ON booking_schema.bookings(client_id);
CREATE INDEX IF NOT EXISTS idx_bookings_scheduled_at ON booking_schema.bookings(scheduled_at);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON booking_schema.bookings(status);
CREATE INDEX IF NOT EXISTS idx_bookings_location_slot ON booking_schema.bookings(location, scheduled_at);
