CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE vehicles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    plate VARCHAR(20) NOT NULL UNIQUE,
    model VARCHAR(100),
    type VARCHAR(20) NOT NULL CHECK (type IN ('MOTORCYCLE', 'CAR', 'VAN', 'TRUCK')),
    capacity NUMERIC(10,2) NOT NULL CHECK (capacity > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'IN_USE', 'MAINTENANCE', 'INACTIVE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_vehicles_organization_id ON vehicles(organization_id);