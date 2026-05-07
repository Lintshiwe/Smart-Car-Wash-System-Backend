-- V1: Initial client_schema — client profile, vehicles, addresses, mandates.

CREATE TABLE IF NOT EXISTS clients (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL UNIQUE,
    first_name      TEXT        NOT NULL,
    last_name       TEXT        NOT NULL,
    phone           TEXT        NOT NULL,
    id_number       TEXT,
    date_of_birth   DATE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_clients_user ON clients (user_id);

CREATE TABLE IF NOT EXISTS vehicles (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id       UUID        NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    make            TEXT        NOT NULL,
    model           TEXT        NOT NULL,
    license_plate   TEXT        NOT NULL,
    vehicle_type    TEXT        NOT NULL CHECK (vehicle_type IN ('SEDAN','SUV','BAKKIE','VAN','OTHER')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_vehicles_client ON vehicles (client_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_vehicles_client_plate ON vehicles (client_id, license_plate);

CREATE TABLE IF NOT EXISTS addresses (
    id              UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id       UUID            NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    street          TEXT            NOT NULL,
    city            TEXT            NOT NULL,
    province        TEXT            NOT NULL,
    postal_code     TEXT,
    latitude        NUMERIC(9,6),
    longitude       NUMERIC(9,6),
    is_primary      BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_addresses_client ON addresses (client_id);

CREATE TABLE IF NOT EXISTS mandates (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id           UUID        NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    mandate_version     TEXT        NOT NULL,
    agreed_full_name    TEXT        NOT NULL,
    ip_address          TEXT,
    pdf_url             TEXT,
    accepted_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_mandates_client ON mandates (client_id);
