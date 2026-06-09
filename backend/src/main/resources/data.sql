-- SEED DATA POINTCHECK - VERSIÓN NORMALIZADA UUID v4
-- Password: 123456 (BCrypt)

-- 1. CATEGORÍAS
INSERT IGNORE INTO categories (id, name, icon_key, color_hex, active, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'Barbería y Estética', 'content_cut', '#FFB74D', 1, NOW()),
('550e8400-e29b-41d4-a716-446655440002', 'Salud y Kinesiología', 'medical_services', '#81C784', 1, NOW()),
('550e8400-e29b-41d4-a716-446655440003', 'Deporte y Fitness', 'fitness_center', '#64B5F6', 1, NOW());

-- 2. USUARIOS
INSERT IGNORE INTO users (id, name, email, password, rut, phone, role, active, created_at) VALUES
('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 'Administrador Sistema', 'admin@pointcheck.cl', '$2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW', '9.999.999-9', '+56900000000', 'ADMIN', 1, NOW()),
('s1s1s1s1-s1s1-s1s1-s1s1-s1s1s1s1s1s1', 'Franco el Barbero', 'franco@barber.cl', '$2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW', '18.111.111-1', '+56911111111', 'SPECIALIST', 1, NOW()),
('s2s2s2s2-s2s2-s2s2-s2s2-s2s2s2s2s2s2', 'Dra. Maria Paz', 'maria@salud.cl', '$2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW', '15.222.222-2', '+56922222222', 'SPECIALIST', 1, NOW()),
('c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'Ismael Jimenez', 'ismael@gmail.com', '$2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW', '20.333.333-3', '+56944444444', 'CLIENT', 1, NOW());

-- 3. PERFILES PROFESIONALES
INSERT IGNORE INTO professional_profiles (id, user_id, category_id, display_name, business_name, specialty, description, address, city, country, working_hours_json, default_session_duration_minutes, active, is_verified, rating, created_at) VALUES
('p1p1p1p1-p1p1-p1p1-p1p1-p1p1p1p1p1p1', 's1s1s1s1-s1s1-s1s1-s1s1-s1s1s1s1s1s1', '550e8400-e29b-41d4-a716-446655440001', 'Franco Studio', 'Franco Barber Co.', 'Barbero Master', 'Cortes clásicos.', 'Av. Providencia 1234', 'Santiago', 'Chile', '{"MONDAY":{"start":"09:00","end":"18:00","isActive":true},"TUESDAY":{"start":"09:00","end":"18:00","isActive":true},"WEDNESDAY":{"start":"09:00","end":"18:00","isActive":true},"THURSDAY":{"start":"09:00","end":"18:00","isActive":true},"FRIDAY":{"start":"09:00","end":"18:00","isActive":true}}', 45, 1, 0, 0.0, NOW());

-- 4. SERVICIOS
INSERT IGNORE INTO services (id, professional_profile_id, name, description, price, duration_minutes, price_unit, is_at_home, active, created_at) VALUES
('f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1', 'p1p1p1p1-p1p1-p1p1-p1p1-p1p1p1p1p1p1', 'Corte de Cabello', 'Corte profesional', 15000.00, 30, 'SESSION', 0, 1, NOW());

-- 5. RESERVAS
INSERT IGNORE INTO reservations (id, client_id, specialist_id, service_id, reservation_start, reservation_end, status, created_at) VALUES
('r1r1r1r1-r1r1-r1r1-r1r1-r1r1r1r1r1r1', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 's1s1s1s1-s1s1-s1s1-s1s1-s1s1s1s1s1s1', 'f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1', DATE_ADD(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 150 MINUTE), 'CONFIRMED', NOW());
