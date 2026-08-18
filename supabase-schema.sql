-- =============================================
-- Coffee Vendor App - Supabase Tables
-- Run this SQL in your Supabase SQL Editor:
-- https://supabase.com/dashboard/project/trxoycjvstwslwueltpb/sql/new
-- =============================================

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY NOT NULL,
    user_id TEXT NOT NULL UNIQUE,
    username TEXT NOT NULL DEFAULT '',
    emp_id TEXT NOT NULL DEFAULT '',
    seat_number TEXT NOT NULL DEFAULT '',
    mobile_number TEXT NOT NULL DEFAULT '',
    password TEXT NOT NULL DEFAULT '',
    photo_uri TEXT,
    favorite_beverages TEXT NOT NULL DEFAULT '',
    is_biometric_enabled BOOLEAN NOT NULL DEFAULT false,
    is_logged_in BOOLEAN NOT NULL DEFAULT false,
    role TEXT NOT NULL DEFAULT 'CUSTOMER',
    access_token TEXT NOT NULL DEFAULT '',
    refresh_token TEXT NOT NULL DEFAULT '',
    access_token_expiry BIGINT NOT NULL DEFAULT 0,
    refresh_token_expiry BIGINT NOT NULL DEFAULT 0
);

ALTER TABLE users REPLICA IDENTITY FULL;

-- 2. BEVERAGES TABLE
CREATE TABLE IF NOT EXISTS beverages (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    price DOUBLE PRECISION NOT NULL DEFAULT 0,
    image_url TEXT NOT NULL DEFAULT '',
    ingredients TEXT NOT NULL DEFAULT '',
    category TEXT NOT NULL DEFAULT 'OTHER',
    drawable_res TEXT NOT NULL DEFAULT 'ic_beverage_coffee',
    has_sugar_option BOOLEAN NOT NULL DEFAULT true,
    is_available BOOLEAN NOT NULL DEFAULT true
);

ALTER TABLE beverages REPLICA IDENTITY FULL;

-- 3. ORDERS TABLE
CREATE TABLE IF NOT EXISTS orders (
    id TEXT PRIMARY KEY NOT NULL,
    user_id TEXT NOT NULL,
    beverage_id TEXT NOT NULL,
    beverage_name TEXT NOT NULL DEFAULT '',
    quantity INTEGER NOT NULL DEFAULT 1,
    location_type TEXT NOT NULL DEFAULT 'WORK_DESK',
    seat_or_row TEXT,
    hall_name TEXT,
    target_time TEXT NOT NULL DEFAULT '',
    recurrence TEXT NOT NULL DEFAULT 'NO_REPEAT',
    created_at TEXT NOT NULL DEFAULT '',
    status TEXT NOT NULL DEFAULT 'RECEIVED',
    special_instructions TEXT
);

ALTER TABLE orders REPLICA IDENTITY FULL;

-- 4. ROW LEVEL SECURITY (open for demo)
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE beverages ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow all access" ON users FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all access" ON beverages FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all access" ON orders FOR ALL USING (true) WITH CHECK (true);

-- 5. SEED DEFAULT VENDOR
INSERT INTO users (id, user_id, username, emp_id, seat_number, mobile_number, password, photo_uri, favorite_beverages, is_biometric_enabled, is_logged_in, role, access_token, refresh_token, access_token_expiry, refresh_token_expiry)
VALUES ('vendor_001', 'vendor', 'Coffee Vendor', 'V001', 'Counter-1', '0000000000', '1234', NULL, '', false, false, 'VENDOR', '', '', 0, 0)
ON CONFLICT (user_id) DO NOTHING;

-- 6. SEED 8 BEVERAGES
INSERT INTO beverages (id, name, description, price, image_url, ingredients, category, drawable_res, has_sugar_option, is_available)
VALUES
    ('1', 'Tea', 'Classic Indian chai tea', 15.0, '', 'Tea leaves,Milk,Water,Sugar', 'TEA', 'ic_beverage_coffee', true, true),
    ('2', 'Green Tea', 'Refreshing green tea', 20.0, '', 'Green tea leaves,Water', 'TEA', 'ic_beverage_green_tea', true, true),
    ('3', 'Badam Milk', 'Rich almond milk drink', 30.0, '', 'Almonds,Milk,Sugar,Saffron', 'MILK', 'ic_beverage_badam_milk', true, true),
    ('4', 'Milk', 'Fresh warm milk', 15.0, '', 'Milk', 'MILK', 'ic_beverage_milk', true, true),
    ('5', 'Dry Ginger Tea', 'Spicy dry ginger tea', 20.0, '', 'Dry ginger,Tea leaves,Water,Sugar', 'TEA', 'ic_beverage_dry_ginger_tea', true, true),
    ('6', 'Black Coffee', 'Strong black coffee without milk', 20.0, '', 'Coffee powder,Water', 'COFFEE', 'ic_beverage_black_coffee', true, true),
    ('7', 'Horlicks', 'Warm Horlicks health drink', 25.0, '', 'Horlicks powder,Milk,Sugar', 'HEALTH_DRINK', 'ic_beverage_horlicks', true, true),
    ('8', 'Boost', 'Energy Boost health drink', 25.0, '', 'Boost powder,Milk,Sugar', 'HEALTH_DRINK', 'ic_beverage_boost', true, true)
ON CONFLICT (id) DO NOTHING;

-- 7. MIGRATION FOR EXISTING TABLES (if you already created them before)
-- Run these if your users table already exists without token columns:
-- ALTER TABLE users ADD COLUMN IF NOT EXISTS access_token TEXT NOT NULL DEFAULT '';
-- ALTER TABLE users ADD COLUMN IF NOT EXISTS refresh_token TEXT NOT NULL DEFAULT '';
-- ALTER TABLE users ADD COLUMN IF NOT EXISTS access_token_expiry BIGINT NOT NULL DEFAULT 0;
-- ALTER TABLE users ADD COLUMN IF NOT EXISTS refresh_token_expiry BIGINT NOT NULL DEFAULT 0;
