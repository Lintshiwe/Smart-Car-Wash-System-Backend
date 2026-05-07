-- INT216D Car Wash — Create all service schemas on first container start.
-- Each microservice owns exactly one schema and never reaches into another.

CREATE SCHEMA IF NOT EXISTS auth_schema;
CREATE SCHEMA IF NOT EXISTS client_schema;
CREATE SCHEMA IF NOT EXISTS booking_schema;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";
