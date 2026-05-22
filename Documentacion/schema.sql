DROP DATABASE IF EXISTS pointcheck_db;

CREATE DATABASE pointcheck_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE pointcheck_db;

-- ============================================================
-- TABLE: users
-- Description:
-- Stores base user information for clients and specialists.
-- The role column determines whether the user acts as CLIENT
-- or SPECIALIST.
-- ============================================================

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    rut VARCHAR(12) NOT NULL,
    phone VARCHAR(255) NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CLIENT',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_rut UNIQUE (rut),

    INDEX idx_users_role (role),
    INDEX idx_users_active (active)
) ENGINE=InnoDB;


-- ============================================================
-- TABLE: categories
-- Description:
-- Stores predefined service categories.
-- ============================================================

CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    icon_key VARCHAR(100) NOT NULL,
    color_hex VARCHAR(7) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_categories_name UNIQUE (name)
) ENGINE=InnoDB;


-- ============================================================
-- TABLE: professional_profiles
-- Description:
-- Stores extended professional information for users with
-- SPECIALIST role.
-- Each specialist user can have only one professional profile.
-- ============================================================

CREATE TABLE professional_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    category_id BIGINT NULL,
    display_name VARCHAR(150) NOT NULL,
    business_name VARCHAR(150) NULL,
    specialty VARCHAR(100) NULL,
    description TEXT NULL,
    address VARCHAR(255) NULL,
    city VARCHAR(100) NULL,
    country VARCHAR(100) NULL,
    default_session_duration_minutes INT NOT NULL DEFAULT 60,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_professional_profiles_user UNIQUE (user_id),

    INDEX idx_professional_profiles_user (user_id),
    INDEX idx_professional_profiles_active (active),
    INDEX idx_professional_profiles_category (category_id),

    CONSTRAINT fk_professional_profiles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_professional_profiles_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) ENGINE=InnoDB;


-- ============================================================
-- TABLE: service_templates
-- Description:
-- Stores predefined service templates for each category.
-- ============================================================

CREATE TABLE service_templates (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    default_price DECIMAL(10,2) NULL,
    default_duration INT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,

    PRIMARY KEY (id),

    CONSTRAINT fk_service_templates_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB;


-- ============================================================
-- TABLE: services
-- Description:
-- Stores services offered by a professional profile.
-- Services are associated with professional_profiles, not
-- directly with users.
-- ============================================================

CREATE TABLE services (
    id BIGINT NOT NULL AUTO_INCREMENT,
    professional_profile_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500) NULL,
    price DECIMAL(10,2) NULL,
    duration_minutes INT NULL,
    price_unit VARCHAR(50) NOT NULL DEFAULT 'SESSION',
    is_at_home BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,

    PRIMARY KEY (id),

    INDEX idx_services_professional_profile (professional_profile_id),
    INDEX idx_services_active (active),

    CONSTRAINT fk_services_professional_profile
        FOREIGN KEY (professional_profile_id)
        REFERENCES professional_profiles(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;


-- ============================================================
-- TABLE: reservations
-- Description:
-- Stores scheduled appointments between a client and a specialist.
-- The service_id links the reservation to a service offered by
-- a professional profile.
-- ============================================================

CREATE TABLE reservations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    client_id BIGINT NOT NULL,
    specialist_id BIGINT NOT NULL,
    service_id BIGINT NULL,
    reservation_start DATETIME(6) NOT NULL,
    reservation_end DATETIME(6) NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    notes VARCHAR(1000) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,

    PRIMARY KEY (id),

    INDEX idx_reservations_client (client_id),
    INDEX idx_reservations_specialist (specialist_id),
    INDEX idx_reservations_service (service_id),
    INDEX idx_reservations_start (reservation_start),
    INDEX idx_reservations_status (status),
    INDEX idx_reservations_specialist_date (specialist_id, reservation_start),
    INDEX idx_reservations_client_date (client_id, reservation_start),

    CONSTRAINT fk_reservations_client
        FOREIGN KEY (client_id)
        REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_reservations_specialist
        FOREIGN KEY (specialist_id)
        REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_reservations_service
        FOREIGN KEY (service_id)
        REFERENCES services(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
) ENGINE=InnoDB;


-- ============================================================
-- TABLE: attentions
-- Description:
-- Stores the real attention/attendance event associated with
-- a reservation. This separates scheduled appointments from
-- actual completed or in-progress service delivery.
-- ============================================================

CREATE TABLE attentions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    specialist_id BIGINT NOT NULL,
    started_at DATETIME(6) NOT NULL,
    finished_at DATETIME(6) NULL,
    duration_minutes INT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    observations VARCHAR(2000) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_attentions_reservation UNIQUE (reservation_id),

    INDEX idx_attentions_specialist_date (specialist_id, started_at),
    INDEX idx_attentions_client (client_id),
    INDEX idx_attentions_status (status),

    CONSTRAINT fk_attentions_reservation
        FOREIGN KEY (reservation_id)
        REFERENCES reservations(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_attentions_client
        FOREIGN KEY (client_id)
        REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_attentions_specialist
        FOREIGN KEY (specialist_id)
        REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;


-- ============================================================
-- TABLE: billing_records
-- Description:
-- Stores external billing/payment tracking records.
-- This table does not represent a real payment gateway.
-- It only records operational payment status.
-- ============================================================

CREATE TABLE billing_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    attention_id BIGINT NULL,
    client_id BIGINT NOT NULL,
    specialist_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'CLP',
    payment_method VARCHAR(50) NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    paid_at DATETIME(6) NULL,
    external_reference VARCHAR(255) NULL,
    notes VARCHAR(1000) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,

    PRIMARY KEY (id),

    INDEX idx_billing_reservation (reservation_id),
    INDEX idx_billing_attention (attention_id),
    INDEX idx_billing_client (client_id),
    INDEX idx_billing_specialist (specialist_id),
    INDEX idx_billing_specialist_date (specialist_id, created_at),
    INDEX idx_billing_status (status),
    INDEX idx_billing_paid_at (paid_at),

    CONSTRAINT fk_billing_reservation
        FOREIGN KEY (reservation_id)
        REFERENCES reservations(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_billing_attention
        FOREIGN KEY (attention_id)
        REFERENCES attentions(id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,

    CONSTRAINT fk_billing_client
        FOREIGN KEY (client_id)
        REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_billing_specialist
        FOREIGN KEY (specialist_id)
        REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;


-- ============================================================
-- TABLE: subscriptions
-- Description:
-- Stores subscription plan information for professional profiles.
-- The subscription belongs to the professional profile, not
-- directly to the user.
-- ============================================================

CREATE TABLE subscriptions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    professional_profile_id BIGINT NOT NULL,
    plan_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,

    PRIMARY KEY (id),

    INDEX idx_subscriptions_professional_profile (professional_profile_id),
    INDEX idx_subscriptions_status (status),
    INDEX idx_subscriptions_end_date (end_date),

    CONSTRAINT fk_subscriptions_professional_profile
        FOREIGN KEY (professional_profile_id)
        REFERENCES professional_profiles(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ============================================================
-- DATA SEEDING: Initial Categories and Templates
-- ============================================================

INSERT INTO categories (name, icon_key, color_hex, created_at) VALUES
('Barbería', 'content_cut', '#FFB74D', NOW()),
('Salud', 'medical_services', '#81C784', NOW()),
('Deporte', 'sports_soccer', '#64B5F6', NOW()),
('Estética', 'face', '#F06292', NOW()),
('Bienestar', 'self_improvement', '#BA68C8', NOW()),
('Hogar', 'home_repair_service', '#A1887F', NOW());

-- Templates for Barbería (ID: 1)
INSERT INTO service_templates (category_id, name, default_price, default_duration) VALUES
(1, 'Corte de Cabello', 10000.00, 30),
(1, 'Corte de Barba', 8000.00, 20),
(1, 'Perfilado de Cejas', 5000.00, 15);

-- Templates for Salud (ID: 2)
INSERT INTO service_templates (category_id, name, default_price, default_duration) VALUES
(2, 'Consulta General', 25000.00, 30),
(2, 'Kinesiología', 35000.00, 60),
(2, 'Nutrición', 30000.00, 45);

-- Templates for Deporte (ID: 3)
INSERT INTO service_templates (category_id, name, default_price, default_duration) VALUES
(3, 'Entrenamiento Personal', 20000.00, 60),
(3, 'Evaluación Física', 15000.00, 30),
(3, 'Masaje Deportivo', 25000.00, 60);

-- Templates for Estética (ID: 4)
INSERT INTO service_templates (category_id, name, default_price, default_duration) VALUES
(4, 'Limpieza Facial', 20000.00, 45),
(4, 'Manicure', 12000.00, 60),
(4, 'Pedicure', 15000.00, 60);

-- Templates for Bienestar (ID: 5)
INSERT INTO service_templates (category_id, name, default_price, default_duration) VALUES
(5, 'Masaje Relajante', 30000.00, 60),
(5, 'Yoga 1 a 1', 25000.00, 60),
(5, 'Meditación Guiada', 15000.00, 30);

-- Templates for Hogar (ID: 6)
INSERT INTO service_templates (category_id, name, default_price, default_duration) VALUES
(6, 'Limpieza Express', 15000.00, 120),
(6, 'Mantenimiento Eléctrico', 25000.00, 60),
(6, 'Gasfitería', 25000.00, 60);
