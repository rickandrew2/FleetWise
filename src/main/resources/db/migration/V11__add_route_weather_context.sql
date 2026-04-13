ALTER TABLE route_logs
    ADD COLUMN weather_condition VARCHAR(40);

ALTER TABLE route_logs
    ADD COLUMN temperature_celsius NUMERIC(5, 2);
