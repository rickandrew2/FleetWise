CREATE TABLE fuel_price_history (
    id UUID PRIMARY KEY,
    fuel_type VARCHAR(30) NOT NULL,
    price_per_liter DECIMAL(8,2) NOT NULL,
    brand VARCHAR(50) NOT NULL,
    effective_date DATE NOT NULL,
    source VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_fuel_price_history_fuel_type
        CHECK (fuel_type IN ('DIESEL', 'GASOLINE_91', 'GASOLINE_95', 'DIESEL_PLUS'))
);

CREATE INDEX idx_fuel_price_history_effective_date
    ON fuel_price_history (effective_date DESC);

CREATE INDEX idx_fuel_price_history_fuel_type_effective_date
    ON fuel_price_history (fuel_type, effective_date DESC);
