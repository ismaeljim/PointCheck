-- H2 compatible schema for tests (UUID IDs as VARCHAR)
-- SET FOREIGN_KEY_CHECKS is MySQL specific, removed for H2
DROP TABLE IF EXISTS billing_records;
DROP TABLE IF EXISTS attentions;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS services;
DROP TABLE IF EXISTS service_templates;
DROP TABLE IF EXISTS subscriptions;
DROP TABLE IF EXISTS professional_profiles;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS global_settings;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    rut VARCHAR(12) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CLIENT',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_rut UNIQUE (rut)
);

CREATE TABLE categories (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    icon_key VARCHAR(100) NOT NULL,
    color_hex VARCHAR(7) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_categories_name UNIQUE (name)
);

CREATE TABLE service_templates (
    id VARCHAR(36) PRIMARY KEY,
    category_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    default_price DECIMAL(10,2),
    default_duration INT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_templates_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE professional_profiles (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    category_id VARCHAR(36),
    display_name VARCHAR(150) NOT NULL,
    business_name VARCHAR(150),
    specialty VARCHAR(100),
    description TEXT,
    address VARCHAR(255),
    city VARCHAR(100),
    country VARCHAR(100),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    rating FLOAT NOT NULL DEFAULT 0.0,
    latitude DOUBLE,
    longitude DOUBLE,
    working_hours_json TEXT,
    default_session_duration_minutes INT NOT NULL DEFAULT 60,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_professional_profiles_user UNIQUE (user_id),
    CONSTRAINT fk_pp_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_pp_cat FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE services (
    id VARCHAR(36) PRIMARY KEY,
    professional_profile_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(10,2),
    duration_minutes INT,
    price_unit VARCHAR(50) NOT NULL DEFAULT 'SESSION',
    is_at_home BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_service_prof FOREIGN KEY (professional_profile_id) REFERENCES professional_profiles(id)
);

CREATE TABLE reservations (
    id VARCHAR(36) PRIMARY KEY,
    client_id VARCHAR(36) NOT NULL,
    specialist_id VARCHAR(36) NOT NULL,
    service_id VARCHAR(36),
    reservation_start TIMESTAMP NOT NULL,
    reservation_end TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_res_cli FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT fk_res_spec FOREIGN KEY (specialist_id) REFERENCES users(id),
    CONSTRAINT fk_res_serv FOREIGN KEY (service_id) REFERENCES services(id)
);

CREATE TABLE audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    performed_by VARCHAR(255) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id VARCHAR(100) NOT NULL,
    details VARCHAR(1000),
    timestamp TIMESTAMP NOT NULL
);

CREATE TABLE global_settings (
    id VARCHAR(36) PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL,
    config_value VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_settings_key UNIQUE (config_key)
);

CREATE TABLE attentions (
    id VARCHAR(36) PRIMARY KEY,
    reservation_id VARCHAR(36) NOT NULL,
    client_id VARCHAR(36) NOT NULL,
    specialist_id VARCHAR(36) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,
    duration_minutes INT,
    status VARCHAR(50) NOT NULL,
    observations VARCHAR(2000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_att_res FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_att_cli FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT fk_att_spec FOREIGN KEY (specialist_id) REFERENCES users(id)
);

CREATE TABLE billing_records (
    id VARCHAR(36) PRIMARY KEY,
    reservation_id VARCHAR(36) NOT NULL,
    attention_id VARCHAR(36),
    client_id VARCHAR(36) NOT NULL,
    specialist_id VARCHAR(36) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'CLP',
    payment_method VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    paid_at TIMESTAMP,
    external_reference VARCHAR(255),
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_bill_res FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_bill_att FOREIGN KEY (attention_id) REFERENCES attentions(id),
    CONSTRAINT fk_bill_cli FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT fk_bill_spec FOREIGN KEY (specialist_id) REFERENCES users(id)
);

CREATE TABLE subscriptions (
    id VARCHAR(36) PRIMARY KEY,
    professional_profile_id VARCHAR(36) NOT NULL,
    plan_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_sub_profile FOREIGN KEY (professional_profile_id) REFERENCES professional_profiles(id)
);

CREATE TABLE notifications (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id)
);
