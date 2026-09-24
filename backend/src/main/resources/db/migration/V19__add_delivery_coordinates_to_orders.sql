ALTER TABLE orders
    ADD COLUMN delivery_latitude NUMERIC(9,6) CHECK (delivery_latitude BETWEEN -90 AND 90),
    ADD COLUMN delivery_longitude NUMERIC(9,6) CHECK (delivery_longitude BETWEEN -180 AND 180),
    ADD CONSTRAINT chk_orders_delivery_coordinates_pair
        CHECK ((delivery_latitude IS NULL) = (delivery_longitude IS NULL));
