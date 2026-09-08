ALTER TABLE products DROP CONSTRAINT IF EXISTS products_sku_key;
ALTER TABLE products ADD CONSTRAINT uk_products_organization_sku UNIQUE (organization_id, sku);
