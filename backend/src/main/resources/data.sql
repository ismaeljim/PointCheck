-- SEED DATA POINTCHECK - VERSIÓN NORMALIZADA UUID v4
-- Password: 123456 (BCrypt: $2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW)

-- 1. CATEGORÍAS
INSERT IGNORE INTO categories (id, name, icon_key, color_hex, active, created_at) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'Barbería y Estética', 'content_cut', '#FFB74D', 1, NOW()),
('550e8400-e29b-41d4-a716-446655440002', 'Salud y Kinesiología', 'medical_services', '#81C784', 1, NOW()),
('550e8400-e29b-41d4-a716-446655440003', 'Deporte y Fitness', 'fitness_center', '#64B5F6', 1, NOW()),
('550e8400-e29b-41d4-a716-446655440004', 'Mascotas y Vet', 'pets', '#FF7043', 1, NOW()),
('550e8400-e29b-41d4-a716-446655440005', 'Psicología', 'psychology', '#9575CD', 1, NOW());

-- 2. USUARIOS
INSERT IGNORE INTO users (id, name, email, password, rut, phone, role, active, created_at) VALUES
('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 'Administrador Sistema', 'admin@pointcheck.cl', '$2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW', '9.999.999-9', '+56900000000', 'ADMIN', 1, NOW()),
('s1s1s1s1-s1s1-s1s1-s1s1-s1s1s1s1s1s1', 'Franco el Barbero', 'franco@barber.cl', '$2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW', '18.111.111-1', '+56911111111', 'SPECIALIST', 1, NOW()),
('s2s2s2s2-s2s2-s2s2-s2s2-s2s2s2s2s2s2', 'Dra. Maria Paz', 'maria@salud.cl', '$2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW', '15.222.222-2', '+56922222222', 'SPECIALIST', 1, NOW()),
('s3s3s3s3-s3s3-s3s3-s3s3-s3s3s3s3s3s3', 'Entrenador Carlos', 'carlos@fitness.cl', '$2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW', '17.333.333-3', '+56933333333', 'SPECIALIST', 1, NOW()),
('c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 'Ismael Jimenez', 'ismael@gmail.com', '$2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW', '20.333.333-3', '+56944444444', 'CLIENT', 1, NOW()),
('c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2', 'Valentina Rojas', 'vale@gmail.com', '$2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW', '19.444.444-4', '+56955555555', 'CLIENT', 1, NOW());

-- 3. PERFILES PROFESIONALES
INSERT IGNORE INTO professional_profiles (id, user_id, category_id, display_name, business_name, specialty, description, address, city, country, working_hours_json, default_session_duration_minutes, active, is_verified, rating, created_at) VALUES
('p1p1p1p1-p1p1-p1p1-p1p1-p1p1p1p1p1p1', 's1s1s1s1-s1s1-s1s1-s1s1-s1s1s1s1s1s1', '550e8400-e29b-41d4-a716-446655440001', 'Franco Studio', 'Franco Barber Co.', 'Barbero Master', 'Cortes clásicos y modernos.', 'Av. Providencia 1234', 'Santiago', 'Chile', '{"MONDAY":{"start":"09:00","end":"18:00","isActive":true},"TUESDAY":{"start":"09:00","end":"18:00","isActive":true},"WEDNESDAY":{"start":"09:00","end":"18:00","isActive":true},"THURSDAY":{"start":"09:00","end":"18:00","isActive":true},"FRIDAY":{"start":"09:00","end":"18:00","isActive":true}}', 45, 1, 1, 4.8, NOW()),
('p2p2p2p2-p2p2-p2p2-p2p2-p2p2p2p2p2p2', 's2s2s2s2-s2s2-s2s2-s2s2-s2s2s2s2s2s2', '550e8400-e29b-41d4-a716-446655440002', 'Centro KinePaz', 'Kinesiología Maria Paz', 'Kinesióloga Integral', 'Rehabilitación deportiva.', 'Calle Salud 567', 'Viña del Mar', 'Chile', '{"MONDAY":{"start":"08:00","end":"17:00","isActive":true},"WEDNESDAY":{"start":"08:00","end":"17:00","isActive":true},"FRIDAY":{"start":"08:00","end":"17:00","isActive":true}}', 60, 1, 1, 5.0, NOW()),
('p3p3p3p3-p3p3-p3p3-p3p3-p3p3p3p3p3p3', 's3s3s3s3-s3s3-s3s3-s3s3-s3s3s3s3s3s3', '550e8400-e29b-41d4-a716-446655440003', 'Carlos Coach', 'Carlos Personal Trainer', 'Musculación y HIIT', 'Entrenamiento personalizado.', 'Gimnasio Power 88', 'Santiago', 'Chile', '{"MONDAY":{"start":"06:00","end":"22:00","isActive":true},"SATURDAY":{"start":"09:00","end":"14:00","isActive":true}}', 60, 1, 0, 4.2, NOW());

-- =========================================================================
-- 4. SERVICES (Corregido el UUID de la clase personalizada)
-- =========================================================================
INSERT IGNORE INTO services (id, professional_profile_id, name, description, price, duration_minutes, price_unit, is_at_home, active, created_at) VALUES
('f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1', 'p1p1p1p1-p1p1-p1p1-p1p1-p1p1p1p1p1p1', 'Corte de Cabello', 'Corte profesional con degradado', 15000.00, 30, 'SESSION', 0, 1, NOW()),
('f2f2f2f2-f2f2-f2f2-f2f2-f2f2f2f2f2f2', 'p1p1p1p1-p1p1-p1p1-p1p1-p1p1p1p1p1p1', 'Barba y Perfilado', 'Diseño de barba con toalla caliente', 8000.00, 20, 'SESSION', 0, 1, NOW()),
('f3f3f3f3-f3f3-f3f3-f3f3-f3f3f3f3f3f3', 'p2p2p2p2-p2p2-p2p2-p2p2-p2p2p2p2p2p2', 'Evaluación Kinésica', 'Evaluación inicial y plan de tratamiento', 35000.00, 60, 'SESSION', 0, 1, NOW()),
('f4f4f4f4-f4f4-f4f4-f4f4-f4f4f4f4f4f4', 'p3p3p3p3-p3p3-p3p3-p3p3-p3p3p3p3p3p3', 'Clase Personalizada', 'Clase 1 a 1 de entrenamiento funcional', 20000.00, 60, 'SESSION', 1, 1, NOW());

-- =========================================================================
-- 5. RESERVATIONS (Ahora la llave foránea encontrará su par correspondiente)
-- =========================================================================
INSERT IGNORE INTO reservations (id, client_id, specialist_id, service_id, reservation_start, reservation_end, status, created_at) VALUES
('r1r1r1r1-r1r1-r1r1-r1r1-r1r1r1r1r1r1', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 's1s1s1s1-s1s1-s1s1-s1s1-s1s1s1s1s1s1', 'f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1', DATE_ADD(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 150 MINUTE), 'CONFIRMED', NOW()),
('r2r2r2r2-r2r2-r2r2-r2r2-r2r2r2r2r2r2', 'c2c2c2c2-c2c2-c2c2-c2c2-c2c2c2c2c2c2', 's2s2s2s2-s2s2-s2s2-s2s2-s2s2s2s2s2s2', 'f3f3f3f3-f3f3-f3f3-f3f3-f3f3f3f3f3f3', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 25 HOUR), 'PENDING', NOW()),
('r3r3r3r3-r3r3-r3r3-r3r3-r3r3r3r3r3r3', 'c1c1c1c1-c1c1-c1c1-c1c1-c1c1c1c1c1c1', 's3s3s3s3-s3s3-s3s3-s3s3-s3s3s3s3s3s3', 'f4f4f4f4-f4f4-f4f4-f4f4-f4f4f4f4f4f4', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 23 HOUR), 'COMPLETED', NOW());

-- 6. PARÁMETROS GLOBALES (Admin Settings)
INSERT IGNORE INTO global_settings (id, config_key, config_value, description) VALUES
('g1g1g1g1-g1g1-g1g1-g1g1-g1g1g1g1g1g1', 'IVA', '19', 'Impuesto al valor agregado (%)'),
('g2g2g2g2-g2g2-g2g2-g2g2-g2g2g2g2g2g2', 'COMMISSION_RATE', '10', 'Comisión cobrada a especialistas (%)'),
('g3g3g3g3-g3g3-g3g3-g3g3-g3g3g3g3g3g3', 'MIN_WITHDRAWAL', '5000', 'Monto mínimo para retiro de fondos');

-- 7. AUDITORÍA (Logs)
INSERT IGNORE INTO audit_logs (id, performed_by_name, performed_by_email, action, target_type, target_id, details, ip_address, timestamp) VALUES
('L1L1L1L1-L1L1-L1L1-L1L1-L1L1L1L1L1L1', 'Administrador Sistema', 'admin@pointcheck.cl', 'LOGIN', 'USER', 'a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', 'Inicio de sesión exitoso', '192.168.100.74', NOW()),
('L2L2L2L2-L2L2-L2L2-L2L2-L2L2L2L2L2L2', 'Administrador Sistema', 'admin@pointcheck.cl', 'UPDATE_SETTING', 'GLOBAL_SETTINGS', 'g1g1g1g1-g1g1-g1g1-g1g1-g1g1g1g1g1g1', 'IVA actualizado de 18 a 19', '192.168.100.74', NOW()),
('L3L3L3L3-L3L3-L3L3-L3L3-L3L3L3L3L3L3', 'Franco el Barbero', 'franco@barber.cl', 'CREATE_SERVICE', 'SERVICE', 'f2f2f2f2-f2f2-f2f2-f2f2-f2f2f2f2f2f2', 'Nuevo servicio: Barba y Perfilado', '192.168.100.50', NOW());
