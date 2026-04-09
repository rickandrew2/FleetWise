CREATE TABLE vehicles (
    id UUID PRIMARY KEY,
    plate_number VARCHAR(20) UNIQUE NOT NULL,
    make VARCHAR(50),
    model VARCHAR(50),
    vehicle_year INT,
    fuel_type VARCHAR(30),
    tank_capacity_liters DECIMAL(6,2),
    epa_vehicle_id INT,
    combined_mpg DECIMAL(6,2),
    city_mpg DECIMAL(6,2),
    highway_mpg DECIMAL(6,2),
    assigned_driver_id UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_vehicles_assigned_driver
        FOREIGN KEY (assigned_driver_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);