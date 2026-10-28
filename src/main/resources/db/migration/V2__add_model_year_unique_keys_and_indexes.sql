-- Anio del vehiculo, indices de busqueda y claves de negocio unicas.
-- La creacion de los indices unicos se bloquea si ya existen duplicados:
-- es preferible que la migracion falle de forma explicita antes que borrar datos.

ALTER TABLE vehicle ADD COLUMN IF NOT EXISTS model_year INTEGER;

ALTER TABLE vehicle ALTER COLUMN price TYPE NUMERIC(12, 2);

CREATE INDEX IF NOT EXISTS idx_vehicle_brand ON vehicle (brand);
CREATE INDEX IF NOT EXISTS idx_vehicle_model ON vehicle (model);
CREATE INDEX IF NOT EXISTS idx_purchase_order_customer ON purchase_order (customer_id);
CREATE INDEX IF NOT EXISTS idx_purchase_order_vehicle ON purchase_order (vehicle_id);
CREATE INDEX IF NOT EXISTS idx_payment_method_purchase_order ON payment_method (purchase_order_id);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM vehicle
        GROUP BY vehicle_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Migracion abortada: existen vehicle_id duplicados en la tabla vehicle. Depure los datos antes de aplicar la clave unica uk_vehicle_vehicle_id.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM purchase_order
        GROUP BY purchase_order_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Migracion abortada: existen purchase_order_id duplicados en la tabla purchase_order. Depure los datos antes de aplicar la clave unica uk_purchase_order_purchase_order_id.';
    END IF;
END
$$;

CREATE UNIQUE INDEX IF NOT EXISTS uk_vehicle_vehicle_id ON vehicle (vehicle_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_purchase_order_purchase_order_id ON purchase_order (purchase_order_id);
