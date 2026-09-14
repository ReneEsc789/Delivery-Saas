ALTER TABLE vehicles DROP CONSTRAINT IF EXISTS vehicles_plate_key;
ALTER TABLE vehicles ADD CONSTRAINT uk_vehicles_organization_plate UNIQUE (organization_id, plate);
