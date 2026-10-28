BEGIN;

INSERT INTO vehicle (id, vehicle_id, model_id, brand, model, model_year, price, currency, available)
SELECT gen_random_uuid(), 'model-101', 'model-101', 'Toyota', 'Corolla', 2024, 28900.00, 'MXN', TRUE
WHERE NOT EXISTS (SELECT 1 FROM vehicle WHERE vehicle_id = 'model-101');

INSERT INTO vehicle (id, vehicle_id, model_id, brand, model, model_year, price, currency, available)
SELECT gen_random_uuid(), 'model-102', 'model-102', 'Honda', 'Civic', 2024, 425000.00, 'MXN', TRUE
WHERE NOT EXISTS (SELECT 1 FROM vehicle WHERE vehicle_id = 'model-102');

INSERT INTO vehicle (id, vehicle_id, model_id, brand, model, model_year, price, currency, available)
SELECT gen_random_uuid(), 'model-103', 'model-103', 'Nissan', 'Versa', 2023, 315000.00, 'MXN', TRUE
WHERE NOT EXISTS (SELECT 1 FROM vehicle WHERE vehicle_id = 'model-103');

INSERT INTO vehicle (id, vehicle_id, model_id, brand, model, model_year, price, currency, available)
SELECT gen_random_uuid(), 'model-104', 'model-104', 'Mazda', 'Mazda 3', 2025, 455000.00, 'MXN', TRUE
WHERE NOT EXISTS (SELECT 1 FROM vehicle WHERE vehicle_id = 'model-104');

INSERT INTO vehicle (id, vehicle_id, model_id, brand, model, model_year, price, currency, available)
SELECT gen_random_uuid(), 'model-105', 'model-105', 'Kia', 'Forte', 2022, 375000.00, 'MXN', FALSE
WHERE NOT EXISTS (SELECT 1 FROM vehicle WHERE vehicle_id = 'model-105');

COMMIT;
