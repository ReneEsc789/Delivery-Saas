CREATE INDEX idx_customers_user_id ON customers(user_id);
CREATE INDEX idx_orders_org_customer ON orders(organization_id, customer_id);
CREATE INDEX idx_deliveries_org_driver ON deliveries(organization_id, driver_id);
CREATE INDEX idx_audit_logs_org_created ON audit_logs(organization_id, created_at DESC);
CREATE INDEX idx_audit_logs_org_action ON audit_logs(organization_id, action);
