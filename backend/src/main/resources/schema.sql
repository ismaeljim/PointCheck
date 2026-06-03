-- 1. LIMPIEZA TOTAL (Orden de dependencias invertido para evitar errores de FK)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS subscriptions;
DROP TABLE IF EXISTS billing_records;
DROP TABLE IF EXISTS attentions;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS services;
DROP TABLE IF EXISTS service_templates;
DROP TABLE IF EXISTS professional_profiles;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS global_settings;
SET FOREIGN_KEY_CHECKS = 1;

-- 2. ESTRUCTURA DE TABLAS (UUID as String)

CREATE TABLE users (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    rut VARCHAR(12) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CLIENT',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_rut UNIQUE (rut)
) ENGINE=InnoDB;

CREATE TABLE categories (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(100) NOT NULL,
    icon_key VARCHAR(100) NOT NULL,
    color_hex VARCHAR(7) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_categories_name UNIQUE (name)
) ENGINE=InnoDB;

CREATE TABLE professional_profiles (
    id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    category_id VARCHAR(36) NULL,
    display_name VARCHAR(150) NOT NULL,
    business_name VARCHAR(150) NULL,
    specialty VARCHAR(100) NULL,
    description TEXT NULL,
    address VARCHAR(255) NULL,
    city VARCHAR(100) NULL,
    country VARCHAR(100) NULL,
    latitude DOUBLE NULL,
    longitude DOUBLE NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    rating FLOAT NOT NULL DEFAULT 0.0,
    working_hours_json TEXT NULL,
    default_session_duration_minutes INT NOT NULL DEFAULT 60,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_professional_profiles_user UNIQUE (user_id),
    CONSTRAINT fk_professional_profiles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_professional_profiles_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB;

CREATE TABLE services (
    id VARCHAR(36) NOT NULL,
    professional_profile_id VARCHAR(36) NOT NULL,
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
    CONSTRAINT fk_services_profile FOREIGN KEY (professional_profile_id) REFERENCES professional_profiles(id)
) ENGINE=InnoDB;

CREATE TABLE reservations (
    id VARCHAR(36) NOT NULL,
    client_id VARCHAR(36) NOT NULL,
    specialist_id VARCHAR(36) NOT NULL,
    service_id VARCHAR(36) NULL,
    reservation_start DATETIME(6) NOT NULL,
    reservation_end DATETIME(6) NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    notes VARCHAR(1000) NULL,
    payment_method VARCHAR(50) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_res_client FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT fk_res_specialist FOREIGN KEY (specialist_id) REFERENCES users(id),
    CONSTRAINT fk_res_service FOREIGN KEY (service_id) REFERENCES services(id)
) ENGINE=InnoDB;

CREATE TABLE attentions (
    id VARCHAR(36) NOT NULL,
    reservation_id VARCHAR(36) NOT NULL,
    client_id VARCHAR(36) NOT NULL,
    specialist_id VARCHAR(36) NOT NULL,
    started_at DATETIME(6) NOT NULL,
    finished_at DATETIME(6) NULL,
    duration_minutes INT NULL,
    status VARCHAR(50) NOT NULL,
    observations VARCHAR(2000) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_attentions_reservation UNIQUE (reservation_id),
    CONSTRAINT fk_att_res FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_att_cli FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT fk_att_spec FOREIGN KEY (specialist_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE billing_records (
    id VARCHAR(36) NOT NULL,
    reservation_id VARCHAR(36) NOT NULL,
    attention_id VARCHAR(36) NULL,
    client_id VARCHAR(36) NOT NULL,
    specialist_id VARCHAR(36) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'CLP',
    payment_method VARCHAR(50) NULL,
    status VARCHAR(50) NOT NULL,
    paid_at DATETIME(6) NULL,
    external_reference VARCHAR(255) NULL,
    notes VARCHAR(1000) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_bill_res FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_bill_att FOREIGN KEY (attention_id) REFERENCES attentions(id),
    CONSTRAINT fk_bill_cli FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT fk_bill_spec FOREIGN KEY (specialist_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE subscriptions (
    id VARCHAR(36) NOT NULL,
    professional_profile_id VARCHAR(36) NOT NULL,
    plan_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_sub_profile FOREIGN KEY (professional_profile_id) REFERENCES professional_profiles(id)
) ENGINE=InnoDB;

CREATE TABLE audit_logs (
    id VARCHAR(36) NOT NULL,
    action VARCHAR(100) NOT NULL,
    performed_by VARCHAR(255) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id VARCHAR(100) NOT NULL,
    details VARCHAR(1000) NULL,
    timestamp DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE global_settings (
    id VARCHAR(36) NOT NULL,
    config_key VARCHAR(100) NOT NULL,
    config_value VARCHAR(255) NOT NULL,
    description VARCHAR(255) NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_settings_key UNIQUE (config_key)
) ENGINE=InnoDB;

CREATE TABLE service_templates (
    id VARCHAR(36) NOT NULL,
    category_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    default_price DECIMAL(10,2) NULL,
    default_duration INT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT fk_templates_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB;
