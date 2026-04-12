UPDATE vehicles
SET fuel_type = CASE
    WHEN fuel_type IS NULL OR btrim(fuel_type) = '' THEN NULL
    WHEN upper(btrim(fuel_type)) IN ('DIESEL', 'DSL') THEN 'DIESEL'
    WHEN upper(btrim(fuel_type)) IN ('DIESEL_PLUS', 'DIESEL PLUS', 'PREMIUM DIESEL', 'DIESEL PREMIUM') THEN 'DIESEL_PLUS'
    WHEN upper(btrim(fuel_type)) IN ('GASOLINE_95', 'GASOLINE 95', 'PREMIUM', 'PREMIUM GASOLINE', 'UNLEADED 95', 'XCS', 'BLAZE') THEN 'GASOLINE_95'
    WHEN upper(btrim(fuel_type)) IN ('GASOLINE_91', 'GASOLINE 91', 'GASOLINE', 'REGULAR', 'REGULAR GASOLINE', 'UNLEADED', 'UNLEADED 91', 'XTRA ADVANCE') THEN 'GASOLINE_91'
    ELSE NULL
END;

ALTER TABLE vehicles
    ADD CONSTRAINT chk_vehicles_fuel_type
    CHECK (fuel_type IS NULL OR fuel_type IN ('DIESEL', 'GASOLINE_91', 'GASOLINE_95', 'DIESEL_PLUS'));
