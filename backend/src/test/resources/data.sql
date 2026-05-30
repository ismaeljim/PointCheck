-- 1. INSERTAR CATEGORÍAS
MERGE INTO categories KEY(id) VALUES ('cat-1', 'Barbería', 'content_cut', '#FFB74D', true, CURRENT_TIMESTAMP);
MERGE INTO categories KEY(id) VALUES ('cat-2', 'Salud', 'medical_services', '#81C784', true, CURRENT_TIMESTAMP);
MERGE INTO categories KEY(id) VALUES ('cat-3', 'Deporte', 'sports_soccer', '#64B5F6', true, CURRENT_TIMESTAMP);
MERGE INTO categories KEY(id) VALUES ('cat-4', 'Estética', 'face', '#F06292', true, CURRENT_TIMESTAMP);
MERGE INTO categories KEY(id) VALUES ('cat-5', 'Bienestar', 'self_improvement', '#BA68C8', true, CURRENT_TIMESTAMP);
MERGE INTO categories KEY(id) VALUES ('cat-6', 'Hogar', 'home_repair_service', '#A1887F', true, CURRENT_TIMESTAMP);

-- 2. INSERTAR USUARIOS (ADMIN, SPECIALISTS, CLIENTS)
MERGE INTO users (id, name, email, password, rut, phone, role, active, created_at) KEY(id) VALUES ('user-1', 'Admin PointCheck', 'admin@pointcheck.cl', '123456', '9.999.999-9', '+56900000000', 'ADMIN', true, CURRENT_TIMESTAMP);
MERGE INTO users (id, name, email, password, rut, phone, role, active, created_at) KEY(id) VALUES ('user-2', 'Franco el Barbero', 'franco@barber.cl', '123456', '18.111.111-1', '+56911111111', 'SPECIALIST', true, CURRENT_TIMESTAMP);
MERGE INTO users (id, name, email, password, rut, phone, role, active, created_at) KEY(id) VALUES ('user-3', 'Dra. Maria Paz', 'maria@salud.cl', '123456', '15.222.222-2', '+56922222222', 'SPECIALIST', true, CURRENT_TIMESTAMP);
MERGE INTO users (id, name, email, password, rut, phone, role, active, created_at) KEY(id) VALUES ('user-4', 'Ismael Jimenez', 'ismael@gmail.com', '123456', '20.333.333-3', '+56933333333', 'CLIENT', true, CURRENT_TIMESTAMP);
MERGE INTO users (id, name, email, password, rut, phone, role, active, created_at) KEY(id) VALUES ('user-5', 'Carla Rojas', 'carla@yahoo.cl', '123456', '21.444.444-4', '+56944444444', 'CLIENT', true, CURRENT_TIMESTAMP);

-- 3. PERFILES PROFESIONALES
MERGE INTO professional_profiles (id, user_id, category_id, display_name, business_name, specialty, description, address, city, country, default_session_duration_minutes, active, created_at) KEY(id) VALUES ('prof-1', 'user-2', 'cat-1', 'Franco Studio', 'Franco Barber Co.', 'Barbero Master', 'Cortes clásicos y modernos con los mejores productos.', 'Av. Providencia 1234', 'Santiago', 'Chile', 45, true, CURRENT_TIMESTAMP);
MERGE INTO professional_profiles (id, user_id, category_id, display_name, business_name, specialty, description, address, city, country, default_session_duration_minutes, active, created_at) KEY(id) VALUES ('prof-2', 'user-3', 'cat-2', 'Dra. Maria Paz', 'Centro Kinesiológico', 'Kinesióloga Deportiva', 'Rehabilitación avanzada para atletas de alto rendimiento.', 'Apoquindo 4500, Of 402', 'Las Condes', 'Chile', 60, true, CURRENT_TIMESTAMP);

-- 4. SERVICIOS PARA LOS ESPECIALISTAS
MERGE INTO services (id, professional_profile_id, name, price, duration_minutes, active, created_at) KEY(id) VALUES ('serv-1', 'prof-1', 'Corte de Cabello', 12000.00, 30, true, CURRENT_TIMESTAMP);
MERGE INTO services (id, professional_profile_id, name, price, duration_minutes, active, created_at) KEY(id) VALUES ('serv-2', 'prof-1', 'Perfilado de Barba', 8000.00, 20, true, CURRENT_TIMESTAMP);
MERGE INTO services (id, professional_profile_id, name, price, duration_minutes, active, created_at) KEY(id) VALUES ('serv-3', 'prof-2', 'Sesión Kinesiología', 35000.00, 60, true, CURRENT_TIMESTAMP);

-- 5. RESERVAS DE PRUEBA (H2 compatible date arithmetic)
MERGE INTO reservations (id, client_id, specialist_id, service_id, reservation_start, status, created_at) KEY(id) VALUES ('res-1', 'user-4', 'user-2', 'serv-1', DATEADD('DAY', 1, CURRENT_TIMESTAMP), 'CONFIRMED', CURRENT_TIMESTAMP);
MERGE INTO reservations (id, client_id, specialist_id, service_id, reservation_start, status, created_at) KEY(id) VALUES ('res-2', 'user-5', 'user-2', 'serv-2', DATEADD('DAY', 2, CURRENT_TIMESTAMP), 'PENDING', CURRENT_TIMESTAMP);
MERGE INTO reservations (id, client_id, specialist_id, service_id, reservation_start, status, created_at) KEY(id) VALUES ('res-3', 'user-4', 'user-3', 'serv-3', DATEADD('DAY', -1, CURRENT_TIMESTAMP), 'COMPLETED', CURRENT_TIMESTAMP);

-- 6. CONFIGURACIONES GLOBALES
MERGE INTO global_settings (id, config_key, config_value, description, updated_at) KEY(id) VALUES ('set-1', 'MAINTENANCE_MODE', 'false', 'Activa la pantalla de mantenimiento para todos los usuarios', CURRENT_TIMESTAMP);
MERGE INTO global_settings (id, config_key, config_value, description, updated_at) KEY(id) VALUES ('set-2', 'MIN_RESERVATION_LEAD_TIME_HOURS', '2', 'Horas mínimas de anticipación para reservar', CURRENT_TIMESTAMP);
MERGE INTO global_settings (id, config_key, config_value, description, updated_at) KEY(id) VALUES ('set-3', 'CURRENCY_CODE', 'CLP', 'Moneda principal del sistema', CURRENT_TIMESTAMP);

-- 7. LOGS DE AUDITORÍA INICIALES
MERGE INTO audit_logs (id, action, performed_by, target_type, target_id, details, timestamp) KEY(id) VALUES ('log-1', 'SYSTEM_STARTUP', 'system', 'SYSTEM', '0', 'Servidor iniciado correctamente y base de datos sembrada', CURRENT_TIMESTAMP);
MERGE INTO audit_logs (id, action, performed_by, target_type, target_id, details, timestamp) KEY(id) VALUES ('log-2', 'MIGRATION', 'admin@pointcheck.cl', 'DATABASE', 'V1', 'Migración de esquema 2.0 completada', CURRENT_TIMESTAMP);
