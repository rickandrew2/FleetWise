CREATE TABLE fuel_logs (
    id UUID PRIMARY KEY,
    vehicle_id UUID NOT NULL,
    driver_id UUID NOT NULL,
    log_date DATE NOT NULL,
    odometer_reading_km DECIMAL(10,2),
    liters_filled DECIMAL(8,2) NOT NULL,
    price_per_liter DECIMAL(10,2) NOT NULL,
    total_cost DECIMAL(12,2) NOT NULL,
    station_name VARCHAR(100),
    station_lat DECIMAL(10,7),
    station_lng DECIMAL(10,7),
    notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_fuel_logs_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_fuel_logs_driver
        FOREIGN KEY (driver_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_fuel_logs_vehicle_id ON fuel_logs(vehicle_id);
CREATE INDEX idx_fuel_logs_driver_id ON fuel_logs(driver_id);
CREATE INDEX idx_fuel_logs_log_date ON fuel_logs(log_date);