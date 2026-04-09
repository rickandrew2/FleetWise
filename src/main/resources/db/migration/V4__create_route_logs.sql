CREATE TABLE route_logs (
    id UUID PRIMARY KEY,
    vehicle_id UUID NOT NULL REFERENCES vehicles(id),
    driver_id UUID NOT NULL REFERENCES users(id),
    trip_date DATE NOT NULL,
    origin_label VARCHAR(150),
    origin_lat NUMERIC(10, 7) NOT NULL,
    origin_lng NUMERIC(10, 7) NOT NULL,
    destination_label VARCHAR(150),
    destination_lat NUMERIC(10, 7) NOT NULL,
    destination_lng NUMERIC(10, 7) NOT NULL,
    distance_km NUMERIC(8, 2) NOT NULL,
    estimated_duration_min INTEGER,
    actual_fuel_used_liters NUMERIC(8, 2),
    expected_fuel_liters NUMERIC(8, 2),
    efficiency_score NUMERIC(5, 2),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX route_logs_vehicle_trip_date_idx ON route_logs(vehicle_id, trip_date DESC);
CREATE INDEX route_logs_driver_trip_date_idx ON route_logs(driver_id, trip_date DESC);
