-- Seed Users (Password is hashed or placeholder, handled via standard encoder later, using a dummy BCrypt hash for 'password')
INSERT INTO users (username, password, email, role) VALUES
('admin', '$2a$10$7QvW0h0h0h0h0h0h0h0h0u3y3y3y3y3y3y3y3y3y3y3y3y3y3y3y3', 'admin@example.com', 'ADMIN'),
('merchant_one', '$2a$10$7QvW0h0h0h0h0h0h0h0h0u3y3y3y3y3y3y3y3y3y3y3y3y3y3y3y3', 'merchant@example.com', 'MERCHANT'),
('agent_john', '$2a$10$7QvW0h0h0h0h0h0h0h0h0u3y3y3y3y3y3y3y3y3y3y3y3y3y3y3y3', 'agent@example.com', 'AGENT'),
('customer_jane', '$2a$10$7QvW0h0h0h0h0h0h0h0h0u3y3y3y3y3y3y3y3y3y3y3y3y3y3y3y3', 'customer@example.com', 'CUSTOMER');

-- Seed Zones
INSERT INTO zones (name) VALUES ('ZONE_NORTH'), ('ZONE_SOUTH'), ('ZONE_EAST'), ('ZONE_WEST');

-- Seed Zone Areas (Pincodes mapping to zones)
INSERT INTO zone_areas (pincode, zone_id) VALUES
('110001', 1),
('110002', 1),
('560001', 2),
('560002', 2),
('700001', 3),
('400001', 4);

-- Seed Rate Cards
INSERT INTO rate_cards (origin_zone, destination_zone, order_type, base_rate, per_kg_rate) VALUES
('ZONE_NORTH', 'ZONE_NORTH', 'STANDARD', 50.00, 10.00),
('ZONE_NORTH', 'ZONE_NORTH', 'EXPRESS', 100.00, 20.00),
('ZONE_NORTH', 'ZONE_SOUTH', 'STANDARD', 80.00, 15.00),
('ZONE_NORTH', 'ZONE_SOUTH', 'EXPRESS', 150.00, 30.00);

-- Seed COD Surcharges
INSERT INTO cod_surcharges (order_type, surcharge_amount) VALUES
('STANDARD', 25.00),
('EXPRESS', 40.00);

-- Seed Agent
INSERT INTO agents (user_id, phone, available) VALUES
(3, '+1234567890', true);
