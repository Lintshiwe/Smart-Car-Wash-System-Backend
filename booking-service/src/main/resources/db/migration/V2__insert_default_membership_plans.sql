-- Flyway migration: V2__insert_default_membership_plans.sql
-- Populates default membership plans

INSERT INTO booking_schema.membership_plans 
(name, description, monthly_price, credits_per_month, free_washes, is_active, discount_eligible, discount_percentage, created_at, updated_at)
VALUES 
-- Basic Plan
('Basic', 'Basic membership with limited credits and benefits', 99.99, 10, 0, true, false, 0.00, NOW(), NOW()),

-- Standard Plan  
('Standard', 'Standard membership with monthly credits and 2 free washes', 199.99, 20, 2, true, true, 5.00, NOW(), NOW()),

-- Premium Plan
('Premium', 'Premium membership with high credit allocation and benefits', 299.99, 40, 5, true, true, 10.00, NOW(), NOW()),

-- VIP Plan
('VIP', 'VIP membership with maximum credits and exclusive perks', 499.99, 80, 10, true, true, 15.00, NOW(), NOW());
