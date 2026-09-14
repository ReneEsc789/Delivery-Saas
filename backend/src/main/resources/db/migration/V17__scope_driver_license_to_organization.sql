ALTER TABLE drivers ADD COLUMN organization_id UUID;

UPDATE drivers d
SET organization_id = u.organization_id
FROM users u
WHERE d.user_id = u.id;

ALTER TABLE drivers ALTER COLUMN organization_id SET NOT NULL;
ALTER TABLE drivers ADD CONSTRAINT fk_drivers_organization
    FOREIGN KEY (organization_id) REFERENCES organizations(id);
ALTER TABLE drivers DROP CONSTRAINT IF EXISTS drivers_license_number_key;
ALTER TABLE drivers ADD CONSTRAINT uk_drivers_organization_license
    UNIQUE (organization_id, license_number);

CREATE INDEX idx_drivers_organization_id ON drivers(organization_id);
