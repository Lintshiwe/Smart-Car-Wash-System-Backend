-- Task 4 seed data for service catalogue

INSERT INTO booking_schema.catalogue_services
(code, name, description, service_type, base_price, duration_minutes, is_active, created_at, updated_at)
VALUES
('BAY_BASIC', 'Basic Bay Wash', 'Exterior wash and dry at bay facility', 'BAY', 399.00, 20, true, NOW(), NOW()),
('BAY_DELUXE', 'Deluxe Bay Wash', 'Exterior wash plus interior vacuum', 'BAY', 649.00, 30, true, NOW(), NOW()),
('BAY_PREMIUM', 'Premium Bay Wash', 'Complete interior and exterior detail', 'BAY', 999.00, 45, true, NOW(), NOW()),
('MOBILE_BASIC', 'Basic Mobile Wash', 'Exterior wash at customer location', 'MOBILE', 399.00, 30, true, NOW(), NOW()),
('MOBILE_DELUXE', 'Deluxe Mobile Wash', 'Exterior plus interior vacuum on-site', 'MOBILE', 649.00, 40, true, NOW(), NOW()),
('MOBILE_PREMIUM', 'Premium Mobile Wash', 'Complete detail at customer location', 'MOBILE', 999.00, 60, true, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

INSERT INTO booking_schema.catalogue_addons
(code, name, description, price, is_active, created_at, updated_at)
VALUES
('TIRE_SHINE', 'Tire Shine', 'Premium tire dressing treatment', 249.00, true, NOW(), NOW()),
('ENGINE_BAY_CLEANING', 'Engine Bay Cleaning', 'Detailed engine bay cleaning', 599.00, true, NOW(), NOW()),
('AIR_FRESHENER_PREMIUM', 'Air Freshener Premium', 'Long-lasting premium fragrance', 199.00, true, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;
