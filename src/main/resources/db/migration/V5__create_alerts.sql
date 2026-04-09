CREATE TABLE alerts (
    id UUID PRIMARY KEY,
    vehicle_id UUID REFERENCES vehicles(id) ON DELETE CASCADE,
    driver_id UUID REFERENCES users(id) ON DELETE CASCADE,
    alert_type VARCHAR(40) NOT NULL,
    message TEXT NOT NULL,
    threshold_value NUMERIC(10, 2),
    actual_value NUMERIC(10, 2),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    triggered_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX alerts_triggered_at_idx ON alerts(triggered_at DESC);
CREATE INDEX alerts_driver_read_idx ON alerts(driver_id, is_read);
