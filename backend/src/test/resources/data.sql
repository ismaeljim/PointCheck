-- 1. INSERTAR CATEGORÍAS
INSERT INTO categories (id, name, icon_key, color_hex, created_at) VALUES
(1, 'Barbería', 'content_cut', '#FFB74D', CURRENT_TIMESTAMP),
(2, 'Salud', 'medical_services', '#81C784', CURRENT_TIMESTAMP),
(3, 'Deporte', 'sports_soccer', '#64B5F6', CURRENT_TIMESTAMP),
(4, 'Estética', 'face', '#F06292', CURRENT_TIMESTAMP),
(5, 'Bienestar', 'self_improvement', '#BA68C8', CURRENT_TIMESTAMP),
(6, 'Hogar', 'home_repair_service', '#A1887F', CURRENT_TIMESTAMP);

-- 2. INSERTAR USUARIOS (ADMIN, SPECIALISTS, CLIENTS)
INSERT INTO users (id, name, email, password, rut, phone, role, active, created_at) VALUES
(1, 'Admin PointCheck', 'admin@pointcheck.cl', '123456', '9.999.999-9', '+56900000000', 'ADMIN', true, CURRENT_TIMESTAMP),
(2, 'Franco el Barbero', 'franco@barber.cl', '123456', '18.111.111-1', '+56911111111', 'SPECIALIST', true, CURRENT_TIMESTAMP),
(3, 'Dra. Maria Paz', 'maria@salud.cl', '123456', '15.222.222-2', '+56922222222', 'SPECIALIST', true, CURRENT_TIMESTAMP),
(4, 'Ismael Jimenez', 'ismael@gmail.com', '123456', '20.333.333-3', '+56933333333', 'CLIENT', true, CURRENT_TIMESTAMP),
(5, 'Carla Rojas', 'carla@yahoo.cl', '123456', '21.444.444-4', '+56944444444', 'CLIENT', true, CURRENT_TIMESTAMP);

-- 3. PERFILES PROFESIONALES
INSERT INTO professional_profiles (id, user_id, category_id, display_name, business_name, specialty, description, address, city, country, default_session_duration_minutes, active, created_at) VALUES
(1, 2, 1, 'Franco Studio', 'Franco Barber Co.', 'Barbero Master', 'Cortes clásicos y modernos con los mejores productos.', 'Av. Providencia 1234', 'Santiago', 'Chile', 45, true, CURRENT_TIMESTAMP),
(2, 3, 2, 'Dra. Maria Paz', 'Centro Kinesiológico', 'Kinesióloga Deportiva', 'Rehabilitación avanzada para atletas de alto rendimiento.', 'Apoquindo 4500, Of 402', 'Las Condes', 'Chile', 60, true, CURRENT_TIMESTAMP);

-- 4. SERVICIOS PARA LOS ESPECIALISTAS
INSERT INTO services (id, professional_profile_id, name, price, duration_minutes, active, created_at) VALUES
(1, 1, 'Corte de Cabello', 12000.00, 30, true, CURRENT_TIMESTAMP),
(2, 1, 'Perfilado de Barba', 8000.00, 20, true, CURRENT_TIMESTAMP),
(3, 2, 'Sesión Kinesiología', 35000.00, 60, true, CURRENT_TIMESTAMP);

-- 5. RESERVAS DE PRUEBA (H2 compatible date arithmetic)
INSERT INTO reservations (client_id, specialist_id, service_id, reservation_start, status, created_at) VALUES
(4, 2, 1, DATEADD('DAY', 1, CURRENT_TIMESTAMP), 'CONFIRMED', CURRENT_TIMESTAMP),
(5, 2, 2, DATEADD('DAY', 2, CURRENT_TIMESTAMP), 'PENDING', CURRENT_TIMESTAMP),
(4, 3, 3, DATEADD('DAY', -1, CURRENT_TIMESTAMP), 'COMPLETED', CURRENT_TIMESTAMP);

-- 6. CONFIGURACIONES GLOBALES
INSERT INTO global_settings (config_key, config_value, description, updated_at) VALUES
('MAINTENANCE_MODE', 'false', 'Activa la pantalla de mantenimiento para todos los usuarios', CURRENT_TIMESTAMP),
('MIN_RESERVATION_LEAD_TIME_HOURS', '2', 'Horas mínimas de anticipación para reservar', CURRENT_TIMESTAMP),
('CURRENCY_CODE', 'CLP', 'Moneda principal del sistema', CURRENT_TIMESTAMP);

-- 7. LOGS DE AUDITORÍA INICIALES
INSERT INTO audit_logs (action, performed_by, target_type, target_id, details, timestamp) VALUES
('SYSTEM_STARTUP', 'system', 'SYSTEM', '0', 'Servidor iniciado correctamente y base de datos sembrada', CURRENT_TIMESTAMP),
('MIGRATION', 'admin@pointcheck.cl', 'DATABASE', 'V1', 'Migración de esquema 2.0 completada', CURRENT_TIMESTAMP);
