-- SEED DATA POINTCHECK - COBERTURA TOTAL CON FRANCO OPTIMIZADO PARA DEMO
-- Password unificado: 123456 (BCrypt)

-- 1. CATEGORÍAS
INSERT IGNORE INTO categories (id, name, icon_key, color_hex, active, created_at) VALUES
('cat-001-barberia', 'Barbería y Estética', 'content_cut', '#FFB74D', 1, NOW()),
('cat-002-salud', 'Salud y Kinesiología', 'medical_services', '#81C784', 1, NOW()),
('cat-003-deporte', 'Deporte y Fitness', 'fitness_center', '#64B5F6', 1, NOW()),
('cat-004-mascotas', 'Mascotas y Vet', 'pets', '#FF7043', 1, NOW()),
('cat-005-psicologia', 'Psicología', 'psychology', '#9575CD', 1, NOW());

-- 2. USUARIOS (Admin, Especialistas y Clientes)
INSERT IGNORE INTO users (id, name, email, password, rut, phone, role, active, created_at) VALUES
('user-admin-001', 'Administrador Sistema', 'admin@pointcheck.cl', '$2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW', '9.999.999-9', '+56900000000', 'ADMIN', 1, NOW()),
('user-spec-001', 'Franco el Barbero', 'franco@barber.cl', '$2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW', '18.111.111-1', '+56911111111', 'SPECIALIST', 1, NOW()),
('user-spec-002', 'Dra. Maria Paz', 'maria@salud.cl', '$2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW', '15.222.222-2', '+56922222222', 'SPECIALIST', 1, NOW()),
('user-spec-003', 'Coach Gonzalo', 'gonzalo@fitness.cl', '$2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW', '16.555.555-5', '+56955555555', 'SPECIALIST', 1, NOW()),
('user-spec-004', 'Dr. Roberto Pelusa', 'roberto@vet.cl', '$2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW', '14.444.444-4', '+56944444444', 'SPECIALIST', 1, NOW()),
('user-spec-005', 'Ps. Laura Contardo', 'laura@psico.cl', '$2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW', '17.888.888-8', '+56988888888', 'SPECIALIST', 1, NOW()),
('user-client-001', 'Ismael Jimenez', 'ismael@gmail.com', '$2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW', '20.333.333-3', '+56966666666', 'CLIENT', 1, NOW()),
('user-client-002', 'Valentina Rojas', 'vale@gmail.com', '$2a$10$q0KM8Tm/OIlCcwMT8Kzj7uVzGzuzllEDx8aGOyQh64zlX6FhkWUYW', '19.444.444-4', '+56977777777', 'CLIENT', 1, NOW());

-- 3. PERFILES PROFESIONALES
INSERT IGNORE INTO professional_profiles (id, user_id, category_id, display_name, business_name, specialty, description, address, city, country, working_hours_json, default_session_duration_minutes, active, is_verified, rating, created_at) VALUES
('prof-001', 'user-spec-001', 'cat-001-barberia', 'Franco Studio', 'Franco Barber Co.', 'Barbero Master', 'Cortes clásicos y modernos.', 'Av. Providencia 1234', 'Santiago', 'Chile', '{"MONDAY":{"start":"09:00","end":"18:00","isActive":true},"TUESDAY":{"start":"09:00","end":"18:00","isActive":true},"WEDNESDAY":{"start":"09:00","end":"18:00","isActive":true},"THURSDAY":{"start":"09:00","end":"18:00","isActive":true},"FRIDAY":{"start":"09:00","end":"18:00","isActive":true},"SATURDAY":{"start":"09:00","end":"18:00","isActive":true}}', 45, 1, 1, 4.8, NOW()),
('prof-002', 'user-spec-002', 'cat-002-salud', 'Kine KAF', 'Centro de Kinesiología Integral', 'Kinesióloga Deportiva', 'Rehabilitación física y masajes.', 'Av. Las Condes 555', 'Santiago', 'Chile', '{"MONDAY":{"start":"08:00","end":"20:00","isActive":true},"TUESDAY":{"start":"08:00","end":"20:00","isActive":true},"WEDNESDAY":{"start":"08:00","end":"20:00","isActive":true},"THURSDAY":{"start":"08:00","end":"20:00","isActive":true},"FRIDAY":{"start":"08:00","end":"20:00","isActive":true},"SATURDAY":{"start":"08:00","end":"20:00","isActive":true}}', 60, 1, 1, 4.7, NOW()),
('prof-003', 'user-spec-003', 'cat-003-deporte', 'G-Fitness', 'Estudio Personalizado Gonzalo', 'Personal Trainer', 'Entrenamiento funcional y calistenia.', 'Gran Avenida 4321', 'La Cisterna', 'Chile', '{"MONDAY":{"start":"07:00","end":"21:00","isActive":true},"TUESDAY":{"start":"07:00","end":"21:00","isActive":true},"WEDNESDAY":{"start":"07:00","end":"21:00","isActive":true},"THURSDAY":{"start":"07:00","end":"21:00","isActive":true},"FRIDAY":{"start":"07:00","end":"21:00","isActive":true},"SATURDAY":{"start":"07:00","end":"21:00","isActive":true}}', 60, 1, 1, 4.9, NOW()),
('prof-004', 'user-spec-004', 'cat-004-mascotas', 'Veterinaria Pelusa', 'Clínica Veterinaria Roberto', 'Cirujano Veterinario', 'Cuidado integral para tus mascotas.', 'Av. Los Pajaritos 99', 'Maipú', 'Chile', '{"MONDAY":{"start":"09:00","end":"20:00","isActive":true},"TUESDAY":{"start":"09:00","end":"20:00","isActive":true},"WEDNESDAY":{"start":"09:00","end":"20:00","isActive":true},"THURSDAY":{"start":"09:00","end":"20:00","isActive":true},"FRIDAY":{"start":"09:00","end":"20:00","isActive":true},"SATURDAY":{"start":"09:00","end":"18:00","isActive":true}}', 30, 1, 1, 4.9, NOW()),
('prof-005', 'user-spec-005', 'cat-005-psicologia', 'Consulta Laura Contardo', 'Espacio Psicológico Vital', 'Psicóloga Clínica', 'Terapia cognitivo-conductual para adultos.', 'Paseo Ahumada 312', 'Santiago', 'Chile', '{"MONDAY":{"start":"09:00","end":"19:00","isActive":true},"TUESDAY":{"start":"09:00","end":"19:00","isActive":true},"WEDNESDAY":{"start":"09:00","end":"19:00","isActive":true},"THURSDAY":{"start":"09:00","end":"19:00","isActive":true},"FRIDAY":{"start":"09:00","end":"19:00","isActive":true},"SATURDAY":{"start":"09:00","end":"19:00","isActive":true}}', 45, 1, 1, 5.0, NOW());

-- 4. CATÁLOGO DE SERVICIOS
INSERT IGNORE INTO services (id, professional_profile_id, name, description, price, duration_minutes, price_unit, is_at_home, active, created_at) VALUES
('serv-001', 'prof-001', 'Corte de Cabello', 'Corte profesional con degradado', 15000.00, 30, 'SESSION', 0, 1, NOW()),
('serv-002', 'prof-002', 'Evaluación Kinésica', 'Diagnóstico y plan de tratamiento', 25000.00, 60, 'SESSION', 0, 1, NOW()),
('serv-003', 'prof-003', 'Clase Personalizada', 'Sesión de entrenamiento 1 a 1', 20000.00, 60, 'SESSION', 0, 1, NOW()),
('serv-004', 'prof-004', 'Consulta Veterinaria', 'Revisión general de salud', 18000.00, 30, 'SESSION', 0, 1, NOW()),
('serv-005', 'prof-005', 'Sesión de Psicoterapia', 'Terapia individual de 45 minutos', 35000.00, 45, 'SESSION', 0, 1, NOW());

-- 5. RESERVACIONES (Optimizadas para Demo el 27 de Junio)
INSERT IGNORE INTO reservations (id, client_id, specialist_profile_id, service_id, reservation_start, reservation_end, status, payment_method, created_at) VALUES
-- Franco: Ayer (25 Jun) -> Completada
('res-franco-001', 'user-client-001', 'prof-001', 'serv-001', '2026-06-25 10:00:00', '2026-06-25 10:30:00', 'COMPLETED', 'CASH', '2026-06-25 08:00:00'),
-- Franco: Hoy (26 Jun) -> Completada
('res-franco-002', 'user-client-002', 'prof-001', 'serv-001', '2026-06-26 15:00:00', '2026-06-26 15:30:00', 'COMPLETED', 'CASH', '2026-06-26 14:00:00'),
-- Franco: Mañana Demo (27 Jun) -> Pendiente
('res-franco-003', 'user-client-001', 'prof-001', 'serv-001', '2026-06-27 09:30:00', '2026-06-27 10:00:00', 'PENDING', 'CASH', NOW()),
-- Franco: Mañana Demo Tarde (27 Jun) -> Pendiente
('res-franco-004', 'user-client-002', 'prof-001', 'serv-001', '2026-06-27 16:00:00', '2026-06-27 16:30:00', 'PENDING', 'CASH', NOW()),

-- OTROS (Para variedad en el sistema)
('res-maria-001', 'user-client-002', 'prof-002', 'serv-002', '2026-07-01 10:00:00', '2026-07-01 11:00:00', 'PENDING', 'TRANSFER', NOW());

-- 6. FACTURACIÓN
INSERT IGNORE INTO billing_records (id, reservation_id, amount, status, payment_method, paid_at, created_at) VALUES
('bill-franco-001', 'res-franco-001', 15000.00, 'PAID', 'CASH', '2026-06-25 10:35:00', '2026-06-25 10:00:00'),
('bill-franco-002', 'res-franco-002', 15000.00, 'PAID', 'CASH', '2026-06-26 15:40:00', '2026-06-26 15:00:00');

-- 7. CONFIGURACIONES GLOBALES
INSERT IGNORE INTO global_settings (id, config_key, config_value, description, updated_at) VALUES
('set-001', 'IVA_PERCENTAGE', '19', 'Porcentaje de IVA aplicado a servicios', NOW()),
('set-002', 'SYSTEM_MAINTENANCE', 'false', 'Estado de mantenimiento global del sistema', NOW()),
('set-003', 'CURRENCY_SYMBOL', '$', 'Símbolo de moneda utilizado en la app', NOW());
