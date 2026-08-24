CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE zones (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE zone_areas (
    id BIGSERIAL PRIMARY KEY,
    pincode VARCHAR(20) NOT NULL UNIQUE,
    zone_id BIGINT NOT NULL REFERENCES zones(id)
);

CREATE TABLE rate_cards (
    id BIGSERIAL PRIMARY KEY,
    origin_zone VARCHAR(100) NOT NULL,
    destination_zone VARCHAR(100) NOT NULL,
    order_type VARCHAR(50) NOT NULL,
    base_rate NUMERIC(10, 2) NOT NULL,
    per_kg_rate NUMERIC(10, 2) NOT NULL
);

CREATE TABLE cod_surcharges (
    id BIGSERIAL PRIMARY KEY,
    order_type VARCHAR(50) NOT NULL UNIQUE,
    surcharge_amount NUMERIC(10, 2) NOT NULL
);

CREATE TABLE agents (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    phone VARCHAR(50) NOT NULL,
    available BOOLEAN NOT NULL
);

CREATE TABLE agent_locations (
    id BIGSERIAL PRIMARY KEY,
    agent_id BIGINT NOT NULL REFERENCES agents(id),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    tracking_number VARCHAR(100) NOT NULL UNIQUE,
    merchant_id BIGINT NOT NULL REFERENCES users(id),
    assigned_agent_id BIGINT REFERENCES agents(id),
    origin_pincode VARCHAR(20) NOT NULL,
    destination_pincode VARCHAR(20) NOT NULL,
    order_type VARCHAR(50) NOT NULL,
    is_cod BOOLEAN NOT NULL,
    weight_kg NUMERIC(10, 2) NOT NULL,
    volumetric_weight_kg NUMERIC(10, 2) NOT NULL,
    billable_weight_kg NUMERIC(10, 2) NOT NULL,
    shipping_cost NUMERIC(10, 2) NOT NULL,
    current_status VARCHAR(50) NOT NULL
);

CREATE TABLE order_status_history (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    status VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    actor_id BIGINT NOT NULL,
    actor_role VARCHAR(50) NOT NULL
);
