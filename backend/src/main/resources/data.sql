-- ==========================================================
-- SEED DATA COMPLETO PARA PRESENTACIÓN (UUID EDITION)
-- ==========================================================

-- 1. CATEGORÍAS
INSERT INTO categories (id, name, icon_key, color_hex, active, created_at) VALUES
('cat-001-barber', 'Barbería', 'content_cut', '#FFB74D', 1, NOW()),
('cat-002-health', 'Salud', 'medical_services', '#81C784', 1, NOW()),
('cat-003-sports', 'Deporte', 'sports_soccer', '#64B5F6', 1, NOW()),
('cat-004-beauty', 'Estética', 'face', '#F06292', 1, NOW());

-- 2. USUARIOS
INSERT INTO users (id, name, email, password, rut, phone, role, active, created_at) VALUES
('u-adm-001', 'Admin PointCheck', 'admin@pointcheck.cl', '123456', '9.999.999-9', '+56900000000', 'ADMIN', 1, NOW()),
('u-spec-001', 'Franco el Barbero', 'franco@barber.cl', '123456', '18.111.111-1', '+56911111111', 'SPECIALIST', 1, NOW()),
('u-spec-002', 'Dra. Maria Paz', 'maria@salud.cl', '123456', '15.222.222-2', '+56922222222', 'SPECIALIST', 1, NOW()),
('u-cli-001', 'Ismael Jimenez', 'ismael@gmail.com', '123456', '20.333.333-3', '+56933333333', 'CLIENT', 1, NOW()),
('u-cli-002', 'Carla Rojas', 'carla@yahoo.cl', '123456', '21.444.444-4', '+56944444444', 'CLIENT', 1, NOW());

-- 3. PERFILES PROFESIONALES
-- Actualización: Franco el Barbero ahora incluye working_hours_json por defecto (Lunes a Viernes 09:00-18:00)
INSERT INTO professional_profiles (id, user_id, category_id, display_name, business_name, specialty, description, address, city, country, default_session_duration_minutes, working_hours_json, active, created_at) VALUES
('prof-001', 'u-spec-001', 'cat-001-barber', 'Franco Studio', 'Franco Barber Co.', 'Barbero Master', 'Cortes clásicos y modernos.', 'Av. Providencia 1234', 'Santiago', 'Chile', 45, '{"MONDAY":{"start":"09:00","end":"18:00","isActive":true},"TUESDAY":{"start":"09:00","end":"18:00","isActive":true},"WEDNESDAY":{"start":"09:00","end":"18:00","isActive":true},"THURSDAY":{"start":"09:00","end":"18:00","isActive":true},"FRIDAY":{"start":"09:00","end":"18:00","isActive":true}}', 1, NOW()),
('prof-002', 'u-spec-002', 'cat-002-health', 'Dra. Maria Paz', 'Centro Kinesiológico', 'Kinesióloga Deportiva', 'Rehabilitación avanzada.', 'Apoquindo 4500', 'Las Condes', 'Chile', 60, NULL, 1, NOW());

-- 4. SUSCRIPCIONES
INSERT INTO subscriptions (id, professional_profile_id, plan_name, status, start_date, end_date, created_at) VALUES
('sub-001', 'prof-001', 'PREMIUM_MONTHLY', 'ACTIVE', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 1 MONTH), NOW()),
('sub-002', 'prof-002', 'BASIC_FREE', 'ACTIVE', CURRENT_DATE, DATE_ADD(CURRENT_DATE, INTERVAL 1 YEAR), NOW());

-- 5. SERVICIOS
INSERT INTO services (id, professional_profile_id, name, description, price, duration_minutes, price_unit, is_at_home, active, created_at) VALUES
('ser-001', 'prof-001', 'Corte de Cabello', 'Corte profesional', 15000.00, 30, 'SESSION', 0, 1, NOW()),
('ser-002', 'prof-001', 'Afeitado Premium', 'Toalla caliente', 10000.00, 20, 'SESSION', 0, 1, NOW()),
('ser-003', 'prof-002', 'Evaluación Kine', 'Sesión inicial', 45000.00, 60, 'SESSION', 1, 1, NOW());

-- 6. PLANTILLAS DE SERVICIO
INSERT INTO service_templates (id, category_id, name, default_price, default_duration, active) VALUES
('temp-001', 'cat-001-barber', 'Corte Simple', 12000.00, 30, 1),
('temp-002', 'cat-002-health', 'Consulta Médica', 35000.00, 20, 1);

-- 7. RESERVAS
INSERT INTO reservations (id, client_id, specialist_id, service_id, reservation_start, reservation_end, status, created_at) VALUES
('res-comp-001', 'u-cli-001', 'u-spec-001', 'ser-001', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 47 HOUR), 'COMPLETED', NOW()),
('res-conf-002', 'u-cli-002', 'u-spec-001', 'ser-002', DATE_ADD(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 150 MINUTE), 'CONFIRMED', NOW()),
('res-pend-003', 'u-cli-001', 'u-spec-002', 'ser-003', DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 25 HOUR), 'PENDING', NOW());

-- 8. ATENCIONES
INSERT INTO attentions (id, reservation_id, client_id, specialist_id, started_at, finished_at, duration_minutes, status, observations, created_at) VALUES
('att-001', 'res-comp-001', 'u-cli-001', 'u-spec-001', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 47 HOUR), 45, 'FINISHED', 'Corte de cabello ejecutado sin problemas.', NOW());

-- 9. FACTURACIÓN
INSERT INTO billing_records (id, reservation_id, attention_id, client_id, specialist_id, amount, currency, status, paid_at, created_at) VALUES
('bill-001', 'res-comp-001', 'att-001', 'u-cli-001', 'u-spec-001', 15000.00, 'CLP', 'PAID', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW());

-- 10. CONFIGURACIONES Y LOGS
INSERT INTO global_settings (id, config_key, config_value, description, updated_at) VALUES
('gs-1', 'MAINTENANCE_MODE', 'false', 'Modo mantenimiento', NOW()),
('gs-2', 'CURRENCY_CODE', 'CLP', 'Moneda local', NOW());

INSERT INTO audit_logs (id, action, performed_by, target_type, target_id, details, timestamp) VALUES
('log-001', 'SYSTEM_INIT', 'system', 'DB', 'INIT', 'Datos de prueba UUID cargados con horarios para Franco', NOW());
