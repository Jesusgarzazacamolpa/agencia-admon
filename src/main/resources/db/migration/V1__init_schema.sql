-- Esquema base del dominio de vehiculos.
-- Idempotente a proposito: permite adoptar Flyway sobre bases creadas antes
-- con ddl-auto=update sin romper el arranque.

CREATE TABLE IF NOT EXISTS vehicle (
    id          UUID           NOT NULL,
    vehicle_id  VARCHAR(100)   NOT NULL,
    model_id    VARCHAR(100)   NOT NULL,
    brand       VARCHAR(100)   NOT NULL,
    model       VARCHAR(100)   NOT NULL,
    price       NUMERIC(12, 2) NOT NULL,
    currency    VARCHAR(3)     NOT NULL,
    available   BOOLEAN        NOT NULL,
    CONSTRAINT pk_vehicle PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS purchase_order (
    id                UUID          NOT NULL,
    purchase_order_id VARCHAR(100)  NOT NULL,
    customer_id       VARCHAR(100)  NOT NULL,
    vehicle_id        VARCHAR(100)  NOT NULL,
    status            VARCHAR(30)   NOT NULL,
    created_at        TIMESTAMP     NOT NULL,
    CONSTRAINT pk_purchase_order PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS payment_method (
    id                    UUID          NOT NULL,
    purchase_order_id     VARCHAR(100)  NOT NULL,
    payment_type          VARCHAR(50)   NOT NULL,
    financial_institution VARCHAR(150)  NOT NULL,
    status                VARCHAR(30)   NOT NULL,
    created_at            TIMESTAMP     NOT NULL,
    CONSTRAINT pk_payment_method PRIMARY KEY (id)
);
